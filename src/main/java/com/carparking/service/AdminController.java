package com.carparking.service;

import com.carparking.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminController {

    private final UserRepository    userRepository;
    private final BookingRepository bookingRepository;

    public AdminController(UserRepository userRepository,
                           BookingRepository bookingRepository) {
        this.userRepository    = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}