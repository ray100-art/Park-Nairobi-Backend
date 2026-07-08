package com.carparking.service;

import com.carparking.model.PaymentStatus;
import com.carparking.model.dto.MpesaPayRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mpesa")
public class MpesaController {

    private final MpesaService      mpesaService;
    private final UserRepository    userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PricingService    pricingService;

    public MpesaController(MpesaService mpesaService,
                             UserRepository userRepository,
                             BookingRepository bookingRepository,
                             PaymentRepository paymentRepository,
                             PricingService pricingService) {
        this.mpesaService       = mpesaService;
        this.userRepository     = userRepository;
        this.bookingRepository  = bookingRepository;
        this.paymentRepository  = paymentRepository;
        this.pricingService     = pricingService;
    }

    @PostMapping("/pay")
    public ResponseEntity<Map<String, Object>> initiatePay(
            @Valid @RequestBody MpesaPayRequest req,
            Authentication auth) {

        Long userId = getUserId(auth);
        var booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!booking.getUserId().equals(userId)) {
            throw new AccessDeniedException("Not your booking");
        }
        if (!"PENDING".equals(booking.getStatus()) || "PAID".equals(booking.getPaymentStatus())) {
            throw new IllegalStateException("Booking is not payable");
        }
        if (paymentRepository.existsByBookingIdAndStatus(booking.getId(), PaymentStatus.PENDING)) {
            throw new IllegalStateException("Payment already in progress");
        }

        var amount = pricingService.expectedAmount(booking);
        Map<String, Object> result = mpesaService.stkPush(
                req.getBookingId(), req.getPhone(), amount);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, String>> callback(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        if (!mpesaService.isAuthenticCallback(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("ResultCode", "1", "ResultDesc", "Forbidden"));
        }
        mpesaService.handleCallback(body);
        return ResponseEntity.ok(Map.of("ResultCode", "0", "ResultDesc", "Success"));
    }

    @GetMapping("/status/{checkoutId}")
    public ResponseEntity<Map<String, Object>> status(
            @PathVariable String checkoutId,
            Authentication auth) {

        Long userId = getUserId(auth);
        return ResponseEntity.ok(mpesaService.queryStatus(checkoutId, userId));
    }

    private Long getUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
