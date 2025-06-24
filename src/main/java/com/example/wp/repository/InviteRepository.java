package com.example.wp.repository;

import com.example.wp.model.Invitation;
import com.example.wp.model.InviteStatus;
import com.example.wp.model.UserEntity;
import com.example.wp.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invitation, Long> {
    boolean existsByInvitedUserAndWorkspaceAndStatus(UserEntity user, Workspace workspace, InviteStatus inviteStatus);

    List<Invitation> findByInvitedUserIdAndStatus(Long userId, InviteStatus status);

    List<Invitation> findByWorkspaceIdAndStatus(Long workspaceId, InviteStatus status);

    Optional<Invitation> findByInvitedUserAndWorkspaceAndStatus(UserEntity user, Workspace workspace, InviteStatus status);
}
