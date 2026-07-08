package com.carparking.service;

import com.carparking.model.Booking;
import com.carparking.model.dto.CreateBookingRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @PostMapping
    public Map<String, Object> createBooking(
            @Valid @RequestBody CreateBookingRequest body,
            Authentication auth) {
        Long userId = getUserId(auth);
        return bookingService.createBooking(
                userId,
                body.getSlotId(),
                body.getVehiclePlate()
        );
    }

    @GetMapping("/my")
    public List<Booking> myBookings(Authentication auth) {
        Long userId = getUserId(auth);
        return bookingService.getUserBookings(userId);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> cancelBooking(
            @PathVariable Long id,
            Authentication auth) {
        Long userId = getUserId(auth);
        return bookingService.cancelBooking(id, userId);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Booking> allBookings() {
        return bookingService.getAllBookings();
    }

    private Long getUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
