package com.carparking.service;

import com.carparking.config.MpesaConfig;
import com.carparking.model.Payment;
import com.carparking.model.PaymentStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

@Service
public class MpesaService {

    private static final Logger log = Logger.getLogger(MpesaService.class.getName());

    private final MpesaConfig       config;
    private final RestTemplate      restTemplate;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PricingService    pricingService;

    @Value("${mpesa.callback.secret:}")
    private String callbackSecret;

    public MpesaService(MpesaConfig config,
                        RestTemplate restTemplate,
                        PaymentRepository paymentRepository,
                        BookingRepository bookingRepository,
                        PricingService pricingService) {
        this.config            = config;
        this.restTemplate      = restTemplate;
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.pricingService    = pricingService;
    }

    public boolean isAuthenticCallback(HttpServletRequest request) {
        if (callbackSecret != null && !callbackSecret.isBlank()) {
            String provided = request.getHeader("X-Mpesa-Callback-Secret");
            return callbackSecret.equals(provided);
        }
        String ip = request.getRemoteAddr();
        return ip.startsWith("196.201.") || ip.startsWith("197.248.");
    }

    public String getAccessToken() {
        String credentials = config.consumerKey + ":" + config.consumerSecret;
        String encoded = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = config.baseUrl + "/oauth/v1/generate?grant_type=client_credentials";

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);
            if (response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "M-Pesa token error", e);
            throw new RuntimeException("Failed to get M-Pesa token");
        }
        throw new RuntimeException("Empty token response from M-Pesa");
    }

    public Map<String, Object> stkPush(Long bookingId, String phone, BigDecimal amount) {
        String normalised = normalisePhone(phone);

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String rawPassword = config.shortcode + config.passkey + timestamp;
        String password = Base64.getEncoder()
                .encodeToString(rawPassword.getBytes(StandardCharsets.UTF_8));

        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = new HashMap<>();
        body.put("BusinessShortCode", config.shortcode);
        body.put("Password",          password);
        body.put("Timestamp",         timestamp);
        body.put("TransactionType",   "CustomerPayBillOnline");
        body.put("Amount",            amount.intValue());
        body.put("PartyA",            normalised);
        body.put("PartyB",            config.shortcode);
        body.put("PhoneNumber",       normalised);
        body.put("CallBackURL",       config.callbackUrl);
        body.put("AccountReference",  "ParkNairobi-" + bookingId);
        body.put("TransactionDesc",   "Parking Payment");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String url = config.baseUrl + "/mpesa/stkpush/v1/processrequest";

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> result = response.getBody();

            if (result != null && "0".equals(String.valueOf(result.get("ResponseCode")))) {
                String checkoutId = (String) result.get("CheckoutRequestID");
                savePendingPayment(bookingId, checkoutId, normalised, amount);
                return Map.of(
                        "success",    true,
                        "message",    "STK Push sent to " + phone + ". Enter your M-Pesa PIN.",
                        "checkoutId", checkoutId
                );
            } else {
                String desc = result != null ? String.valueOf(result.get("ResponseDescription")) : "Unknown error";
                return Map.of("success", false, "message", "M-Pesa error: " + desc);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "STK push error", e);
            return Map.of("success", false, "message", "STK Push failed. Please try again.");
        }
    }

    public void handleCallback(Map<String, Object> callbackData) {
        try {
            Map<String, Object> body = (Map<String, Object>)
                    ((Map<String, Object>) callbackData.get("Body")).get("stkCallback");

            String checkoutId = (String) body.get("CheckoutRequestID");
            int resultCode = ((Number) body.get("ResultCode")).intValue();

            Optional<Payment> opt = paymentRepository.findByCheckoutRequestId(checkoutId);
            if (opt.isEmpty()) return;

            Payment payment = opt.get();
            if (payment.getStatus() == PaymentStatus.COMPLETED) return;
            if (payment.getStatus() != PaymentStatus.PENDING) return;

            if (resultCode == 0) {
                Map<String, Object> meta = (Map<String, Object>) body.get("CallbackMetadata");
                var items = (List<Map<String, Object>>) meta.get("Item");

                String     mpesaRef  = getMetaValue(items, "MpesaReceiptNumber");
                BigDecimal paidAmt   = new BigDecimal(String.valueOf(getMetaValue(items, "Amount")));
                String     phonePaid = getMetaValue(items, "PhoneNumber");

                var booking = bookingRepository.findById(payment.getBookingId()).orElse(null);
                BigDecimal expected = booking != null
                        ? pricingService.expectedAmount(booking) : payment.getAmount();

                if (expected != null && paidAmt.compareTo(expected) != 0) {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setFailureReason("Amount mismatch");
                    paymentRepository.save(payment);
                    return;
                }

                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setMpesaReceiptNumber(mpesaRef);
                payment.setAmount(paidAmt);
                payment.setPhone(phonePaid);
                payment.setCompletedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                if (booking != null) {
                    booking.setStatus("ACTIVE");
                    booking.setPaymentStatus("PAID");
                    bookingRepository.save(booking);
                }

            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason(String.valueOf(body.get("ResultDesc")));
                paymentRepository.save(payment);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Callback parse error: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> queryStatus(String checkoutId, Long userId) {
        Optional<Payment> opt = paymentRepository.findByCheckoutRequestId(checkoutId);
        if (opt.isEmpty()) return Map.of("success", false, "message", "Payment not found");

        Payment p = opt.get();
        var booking = bookingRepository.findById(p.getBookingId()).orElse(null);
        if (booking == null || !booking.getUserId().equals(userId)) {
            return Map.of("success", false, "message", "Unauthorized");
        }

        return Map.of(
                "success",     true,
                "status",      p.getStatus().name(),
                "amount",      p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO,
                "receipt",     p.getMpesaReceiptNumber() != null ? p.getMpesaReceiptNumber() : "",
                "phone",       p.getPhone() != null ? p.getPhone() : "",
                "completedAt", p.getCompletedAt() != null ? p.getCompletedAt().toString() : ""
        );
    }

    private void savePendingPayment(Long bookingId, String checkoutId,
                                    String phone, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setCheckoutRequestId(checkoutId);
        payment.setPhone(phone);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    private String normalisePhone(String phone) {
        phone = phone.replaceAll("\\s+", "").replaceAll("[^\\d+]", "");
        if (phone.startsWith("+254")) return phone.substring(1);
        if (phone.startsWith("0"))   return "254" + phone.substring(1);
        if (phone.startsWith("254")) return phone;
        return "254" + phone;
    }

    private String getMetaValue(List<Map<String, Object>> items, String name) {
        return items.stream()
                .filter(i -> name.equals(i.get("Name")))
                .map(i -> String.valueOf(i.get("Value")))
                .findFirst().orElse("");
    }
}
