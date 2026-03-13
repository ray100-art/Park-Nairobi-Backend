package com.carparking.service;

import com.carparking.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── GET /api/profile ──────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null)
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorised"));

        Optional<User> opt = userRepository.findByEmail(userDetails.getUsername());
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));

        User user = opt.get();
        return ResponseEntity.ok(Map.of(
                "id",       user.getId(),
                "fullName", user.getFullName()  != null ? user.getFullName()  : "",
                "email",    user.getEmail()     != null ? user.getEmail()     : "",
                "phone",    user.getPhone()     != null ? user.getPhone()     : "",
                "role",     user.getRole()      != null ? user.getRole()      : "DRIVER"
        ));
    }

    // ── PUT /api/profile ──────────────────────────────────────────────────
    @PutMapping
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> req) {

        if (userDetails == null)
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorised"));

        Optional<User> opt = userRepository.findByEmail(userDetails.getUsername());
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));

        User   user  = opt.get();
        String name  = req.getOrDefault("fullName", "").trim();
        String email = req.getOrDefault("email",    "").trim().toLowerCase();
        String phone = req.getOrDefault("phone",    "").trim();

        if (name.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Full name is required"));
        if (email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));

        // If email changed, make sure it's not taken by another account
        if (!email.equalsIgnoreCase(user.getEmail())) {
            Optional<User> existing = userRepository.findByEmail(email);
            if (existing.isPresent() && !existing.get().getId().equals(user.getId()))
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Email already in use by another account"));
        }

        user.setFullName(name);
        user.setEmail(email);
        if (!phone.isBlank()) user.setPhone(phone);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "success",  true,
                "message",  "Profile updated successfully",
                "id",       user.getId(),
                "fullName", user.getFullName(),
                "email",    user.getEmail(),
                "phone",    user.getPhone() != null ? user.getPhone() : "",
                "role",     user.getRole()  != null ? user.getRole()  : "DRIVER"
        ));
    }

    // ── PUT /api/profile/password ─────────────────────────────────────────
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> req) {

        if (userDetails == null)
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorised"));

        String currentPassword = req.getOrDefault("currentPassword", "");
        String newPassword     = req.getOrDefault("newPassword",     "");

        if (currentPassword.isBlank() || newPassword.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Both current and new password are required"));
        if (newPassword.length() < 8)
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "New password must be at least 8 characters"));
        if (!newPassword.matches(".*\\d.*"))
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "New password must contain at least one number"));

        Optional<User> opt = userRepository.findByEmail(userDetails.getUsername());
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));

        User user = opt.get();
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash()))
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Current password is incorrect"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Password changed successfully"));
    }
}