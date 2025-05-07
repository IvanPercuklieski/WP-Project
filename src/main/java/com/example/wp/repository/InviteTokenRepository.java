package com.example.wp.repository;

import com.example.wp.model.InviteToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InviteTokenRepository extends JpaRepository<InviteToken, Long> {
    Optional<InviteToken> findByToken(String token);

    boolean existsByWorkspaceIdAndExpiresAtAfter(Long workspaceId, java.time.Instant now);

}
