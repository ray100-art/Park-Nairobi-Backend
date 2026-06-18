package com.carparking.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/mpesa")
public class MpesaController {

    private final MpesaService mpesaService;

    public MpesaController(MpesaService mpesaService) {
        this.mpesaService = mpesaService;
    }

    // POST /api/mpesa/pay
    // Body: { "bookingId": 1, "phone": "0712345678", "amount": 100 }
    @PostMapping("/pay")
    public ResponseEntity<Map<String, Object>> initiatePay(
            @RequestBody Map<String, Object> req) {

        Long       bookingId = Long.valueOf(String.valueOf(req.get("bookingId")));
        String     phone     = String.valueOf(req.get("phone"));
        BigDecimal amount    = new BigDecimal(String.valueOf(req.get("amount")));

        Map<String, Object> result = mpesaService.stkPush(bookingId, phone, amount);
        return ResponseEntity.ok(result);
    }

    // POST /api/mpesa/callback — public, no JWT (Safaricom calls this)
    @PostMapping("/callback")
    public ResponseEntity<Map<String, String>> callback(
            @RequestBody Map<String, Object> body) {

        mpesaService.handleCallback(body);
        return ResponseEntity.ok(Map.of("ResultCode", "0", "ResultDesc", "Success"));
    }

    // GET /api/mpesa/status/{checkoutId} — frontend polls this
    @GetMapping("/status/{checkoutId}")
    public ResponseEntity<Map<String, Object>> status(
            @PathVariable String checkoutId) {

        return ResponseEntity.ok(mpesaService.queryStatus(checkoutId));
    }

}