package com.carparking.service;

import com.carparking.service.JwtService;
import com.carparking.model.User;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository  userRepository;
    private final JwtService      jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       JwtService jwtService,
                       @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.jwtService      = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Register ────────────────────────────────
    public Map<String, Object> register(RegisterRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Unable to register with these details");
        }
        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole("DRIVER");
        user.setActive(true);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return Map.of(
                "success", true,
                "message", "Registration successful",
                "token",   token,
                "user",    sanitizeUser(user)
        );
    }

    // ── Login ────────────────────────────────────
    public Map<String, Object> login(LoginRequest req) {
        Optional<User> opt = userRepository.findByEmail(req.getEmail());
        if (opt.isEmpty()) {
            throw new BadCredentialsException("Invalid email or password");
        }
        User user = opt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }
        String token = jwtService.generateToken(user);
        return Map.of(
                "success", true,
                "message", "Login successful",
                "token",   token,
                "user",    sanitizeUser(user)
        );
    }

    private Map<String, Object> sanitizeUser(User user) {
        return Map.of(
                "id",       user.getId(),
                "fullName", user.getFullName(),
                "email",    user.getEmail(),
                "phone",    user.getPhone(),
                "role",     user.getRole()
        );
    }

    // ── Request DTOs ─────────────────────────────

    public static class RegisterRequest {

        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Phone is required")
        private String phone;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = ".*\\d.*",
                message = "Password must contain at least one number")
        private String password;

        public String getFullName()            { return fullName; }
        public String getEmail()               { return email; }
        public String getPhone()               { return phone; }
        public String getPassword()            { return password; }
        public void setFullName(String v)      { this.fullName = v; }
        public void setEmail(String v)         { this.email = v; }
        public void setPhone(String v)         { this.phone = v; }
        public void setPassword(String v)      { this.password = v; }
    }

    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        public String getEmail()               { return email; }
        public String getPassword()            { return password; }
        public void setEmail(String v)         { this.email = v; }
        public void setPassword(String v)      { this.password = v; }
    }
}