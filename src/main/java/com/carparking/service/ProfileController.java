package com.carparking.service;

import com.carparking.model.User;
import com.carparking.model.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping
    public ResponseEntity<?> getProfile(
            @AuthenticationPrincipal String email) {

        if (email == null)
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorised"));

        Optional<User> opt = userRepository.findByEmail(email);
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

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody UpdateProfileRequest req) {

        if (email == null)
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorised"));

        Optional<User> opt = userRepository.findByEmail(email);
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));

        User user = opt.get();
        user.setFullName(req.getFullName().trim());
        user.setPhone(req.getPhone().trim());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "success",  true,
                "message",  "Profile updated successfully",
                "id",       user.getId(),
                "fullName", user.getFullName(),
                "email",    user.getEmail(),
                "phone",    user.getPhone(),
                "role",     user.getRole()
        ));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal String email,
            @RequestBody Map<String, String> req) {

        if (email == null)
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

        Optional<User> opt = userRepository.findByEmail(email);
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));

        User user = opt.get();
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash()))
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Current password is incorrect"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully. Please sign in again."));
    }
}
