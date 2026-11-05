
package com.settribe.leavemanagement.repository;

import com.settribe.leavemanagement.PasswordResetToken;
import com.settribe.leavemanagement.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void deleteByUser(User user);
}
