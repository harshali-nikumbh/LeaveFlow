
package com.settribe.leavemanagement.service;

import com.settribe.leavemanagement.PasswordResetToken;
import com.settribe.leavemanagement.User;
import com.settribe.leavemanagement.repository.PasswordResetTokenRepository;
import com.settribe.leavemanagement.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void requestPasswordReset(String email) {

        userRepository.findByEmail(email.trim().toLowerCase())
                .ifPresent(user -> {

                    tokenRepository.deleteByUser(user);

                    byte[] randomBytes = new byte[32];
                    secureRandom.nextBytes(randomBytes);

                    String rawToken = Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(randomBytes);

                    String tokenHash = hashToken(rawToken);

                    PasswordResetToken resetToken = new PasswordResetToken(
                            user,
                            tokenHash,
                            LocalDateTime.now().plusMinutes(15));

                    tokenRepository.save(resetToken);

                    String resetLink = frontendUrl
                            + "/reset-password?token=" + rawToken;

                    emailService.sendPasswordResetEmail(
                            user.getEmail(),
                            resetLink);
                });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {

        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = tokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid or expired reset link."));

        if (resetToken.isUsed()
                || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Invalid or expired reset link.");
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to process reset token.",
                    exception);
        }
    }
}
