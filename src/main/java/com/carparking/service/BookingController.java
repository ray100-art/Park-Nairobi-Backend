package com.carparking.service;

import com.carparking.model.Booking;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService,
                             UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    // ── POST /api/bookings ────────────────────────────
    @PostMapping
    public Map<String, Object> createBooking(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        Long userId = getUserId(auth);
        return bookingService.createBooking(
                userId,
                body.get("slotId"),
                body.get("vehiclePlate")
        );
    }

    // ── GET /api/bookings/my ──────────────────────────
    @GetMapping("/my")
    public List<Booking> myBookings(Authentication auth) {
        Long userId = getUserId(auth);
        return bookingService.getUserBookings(userId);
    }

    // ── DELETE /api/bookings/{id} ─────────────────────
    @DeleteMapping("/{id}")
    public Map<String, Object> cancelBooking(
            @PathVariable Long id,
            Authentication auth) {
        Long userId = getUserId(auth);
        return bookingService.cancelBooking(id, userId);
    }

    // ── GET /api/bookings/all (admin only) ────────────
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Booking> allBookings() {
        return bookingService.getAllBookings();
    }

    // ── Helper: get user ID from JWT ──────────────────
    private Long getUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}