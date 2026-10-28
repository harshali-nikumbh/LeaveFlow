
package com.settribe.leavemanagement.controller;

import com.settribe.leavemanagement.User;
import com.settribe.leavemanagement.service.AuthService;
import com.settribe.leavemanagement.service.JwtService;
import com.settribe.leavemanagement.repository.UserRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            AuthService authService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record AuthResponse(
            Long id,
            String name,
            String email,
            String role,
            String token) {
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(
                    request.name().trim(),
                    request.email().trim().toLowerCase(),
                    request.password());

            String token = jwtService.generateToken(
                    user.getEmail(), user.getRole());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getRole(),
                            token));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        String email = request.email().trim().toLowerCase();

        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(
                        request.password(), user.getPassword()))
                .<ResponseEntity<?>>map(user -> {
                    String token = jwtService.generateToken(
                            user.getEmail(), user.getRole());

                    return ResponseEntity.ok(
                            new AuthResponse(
                                    user.getId(),
                                    user.getName(),
                                    user.getEmail(),
                                    user.getRole(),
                                    token));
                })
                .orElseGet(() -> ResponseEntity.status(
                        HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "Invalid email or password.")));
    }
}
