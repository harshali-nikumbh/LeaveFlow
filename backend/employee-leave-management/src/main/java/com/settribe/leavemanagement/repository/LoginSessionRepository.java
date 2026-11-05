
package com.settribe.leavemanagement.repository;

import com.settribe.leavemanagement.LoginSession;
import com.settribe.leavemanagement.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginSessionRepository
        extends JpaRepository<LoginSession, Long> {

    Optional<LoginSession> findByTokenHashAndRevokedFalse(
            String tokenHash);

    void deleteByUser(User user);
}
