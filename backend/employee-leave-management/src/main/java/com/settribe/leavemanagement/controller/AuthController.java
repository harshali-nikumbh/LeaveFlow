
package com.settribe.leavemanagement.controller;

import com.settribe.leavemanagement.User;
import com.settribe.leavemanagement.service.AuthService;
import com.settribe.leavemanagement.service.PasswordResetService;
import com.settribe.leavemanagement.service.SessionService;
import com.settribe.leavemanagement.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final AuthService authService;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final SessionService sessionService;
        private final PasswordResetService passwordResetService;

        @Value("${app.cookie.secure:false}")
        private boolean secureCookie;

        public AuthController(
                        AuthService authService,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        SessionService sessionService,
                        PasswordResetService passwordResetService) {

                this.authService = authService;
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.sessionService = sessionService;
                this.passwordResetService = passwordResetService;
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

        public record ForgotPasswordRequest(
                        @NotBlank @Email String email) {
        }

        public record ResetPasswordRequest(
                        @NotBlank String token,
                        @NotBlank @Size(min = 8, max = 72) String newPassword) {
        }

        public record AuthResponse(
                        Long id,
                        String name,
                        String email,
                        String role) {
        }

        private ResponseCookie createSessionCookie(String token) {
                return ResponseCookie.from("LF_SESSION", token)
                                .httpOnly(true)
                                .secure(secureCookie)
                                .sameSite(secureCookie ? "None" : "Lax")
                                .path("/")
                                .maxAge(Duration.ofHours(1))
                                .build();
        }

        @PostMapping("/register")
        public ResponseEntity<?> register(
                        @Valid @RequestBody RegisterRequest request) {

                try {
                        User user = authService.register(
                                        request.name().trim(),
                                        request.email().trim().toLowerCase(),
                                        request.password());

                        String sessionToken = sessionService.createSession(user);

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .header(HttpHeaders.SET_COOKIE,
                                                        createSessionCookie(sessionToken).toString())
                                        .body(new AuthResponse(
                                                        user.getId(),
                                                        user.getName(),
                                                        user.getEmail(),
                                                        user.getRole()));

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

                                        String sessionToken = sessionService.createSession(user);

                                        return ResponseEntity.ok()
                                                        .header(HttpHeaders.SET_COOKIE,
                                                                        createSessionCookie(sessionToken).toString())
                                                        .body(new AuthResponse(
                                                                        user.getId(),
                                                                        user.getName(),
                                                                        user.getEmail(),
                                                                        user.getRole()));
                                })
                                .orElseGet(() -> ResponseEntity.status(
                                                HttpStatus.UNAUTHORIZED)
                                                .body(Map.of(
                                                                "error", "Invalid email or password.")));
        }

        @PostMapping("/logout")
        public ResponseEntity<?> logout(HttpServletRequest request) {

                if (request.getCookies() != null) {
                        for (var cookie : request.getCookies()) {
                                if ("LF_SESSION".equals(cookie.getName())) {
                                        sessionService.revokeSession(cookie.getValue());
                                        break;
                                }
                        }
                }

                ResponseCookie clearCookie = ResponseCookie.from("LF_SESSION", "")
                                .httpOnly(true)
                                .secure(secureCookie)
                                .sameSite(secureCookie ? "None" : "Lax")
                                .path("/")
                                .maxAge(Duration.ZERO)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                                .body(Map.of("message", "Logged out successfully."));
        }

        @PostMapping("/forgot-password")
        public ResponseEntity<?> forgotPassword(
                        @Valid @RequestBody ForgotPasswordRequest request) {

                passwordResetService.requestPasswordReset(
                                request.email().trim().toLowerCase());

                return ResponseEntity.ok(Map.of(
                                "message",
                                "If an account exists for that email, a password reset link has been sent."));
        }

        @PostMapping("/reset-password")
        public ResponseEntity<?> resetPassword(
                        @Valid @RequestBody ResetPasswordRequest request) {

                try {
                        passwordResetService.resetPassword(
                                        request.token(),
                                        request.newPassword());

                        return ResponseEntity.ok(Map.of(
                                        "message", "Your password has been reset successfully."));

                } catch (IllegalArgumentException ex) {
                        return ResponseEntity.badRequest().body(
                                        Map.of("error", ex.getMessage()));
                }
        }

        @GetMapping("/me")
        public ResponseEntity<?> getCurrentUser(Authentication authentication) {

                if (authentication == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of("error", "Not authenticated."));
                }

                return userRepository.findByEmail(authentication.getName())
                                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(
                                                new AuthResponse(
                                                                user.getId(),
                                                                user.getName(),
                                                                user.getEmail(),
                                                                user.getRole())))
                                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                .body(Map.of("error", "User not found.")));
        }

        @GetMapping("/csrf")
        public ResponseEntity<?> csrf(CsrfToken csrfToken) {
                return ResponseEntity.ok(
                                Map.of("token", csrfToken.getToken()));
        }
}
