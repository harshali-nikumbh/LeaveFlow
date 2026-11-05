
package com.settribe.leavemanagement;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_sessions", indexes = @Index(name = "idx_session_token_hash", columnList = "token_hash"))
public class LoginSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    public LoginSession() {
    }

    public LoginSession(User user, String tokenHash,
            LocalDateTime expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
