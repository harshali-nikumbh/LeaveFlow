
package com.settribe.leavemanagement.service;

import com.settribe.leavemanagement.LoginSession;
import com.settribe.leavemanagement.User;
import com.settribe.leavemanagement.repository.LoginSessionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class SessionService {

    private final LoginSessionRepository sessionRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionService(LoginSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public String createSession(User user) {

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String tokenHash = hashToken(rawToken);

        LoginSession session = new LoginSession(
                user,
                tokenHash,
                LocalDateTime.now().plusHours(1));

        sessionRepository.save(session);

        return rawToken;
    }

    @Transactional(readOnly = true)
    public Optional<User> findValidUser(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }

        String tokenHash = hashToken(rawToken);

        return sessionRepository
                .findByTokenHashAndRevokedFalse(tokenHash)
                .filter(session -> session.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(LoginSession::getUser);
    }

    @Transactional
    public void revokeSession(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        String tokenHash = hashToken(rawToken);

        sessionRepository
                .findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(session -> {
                    session.setRevoked(true);
                    sessionRepository.save(session);
                });
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to process session token.",
                    exception);
        }
    }
}
