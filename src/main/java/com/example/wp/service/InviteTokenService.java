package com.example.wp.service;

import com.example.wp.model.InviteToken;
import com.example.wp.model.MembershipRole;
import com.example.wp.model.UserEntity;
import com.example.wp.model.Workspace;
import com.example.wp.repository.InviteTokenRepository;
import com.example.wp.repository.WorkspaceRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class InviteTokenService {
    private final InviteTokenRepository inviteTokenRepository;
    private final WorkspaceRepository workspaceRepository;

    public InviteTokenService(InviteTokenRepository inviteTokenRepository, WorkspaceRepository workspaceRepository) {
        this.inviteTokenRepository = inviteTokenRepository;
        this.workspaceRepository = workspaceRepository;
    }
    public Optional<InviteToken> getValidTokenForWorkspace(Long workspaceId) {
        return inviteTokenRepository.findAll().stream()
                .filter(token -> token.getWorkspace().getId().equals(workspaceId) && token.getExpiresAt().isAfter(Instant.now()))
                .findFirst();
    }

    public void createInviteToken(Workspace workspace) {
        InviteToken token = new InviteToken();
        token.setToken(UUID.randomUUID().toString());
        token.setWorkspace(workspace);
        token.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        inviteTokenRepository.save(token);
    }

    public Optional<InviteToken> findByToken(String token) {
        return inviteTokenRepository.findByToken(token)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()));
    }

}
