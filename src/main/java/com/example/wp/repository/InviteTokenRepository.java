package com.example.wp.repository;

import com.example.wp.model.InviteToken;
import com.example.wp.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InviteTokenRepository extends JpaRepository<InviteToken, Long> {
    Optional<InviteToken> findByToken(String token);

    boolean existsByWorkspaceIdAndExpiresAtAfter(Long workspaceId, java.time.Instant now);

    @Modifying
    @Query("DELETE FROM InviteToken it WHERE it.workspace = :workspace")
    void deleteByWorkspace(@Param("workspace") Workspace workspace);
}
