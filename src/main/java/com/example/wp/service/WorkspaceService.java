package com.example.wp.service;

import com.example.wp.model.MembershipRole;
import com.example.wp.model.Workspace;
import com.example.wp.repository.InviteTokenRepository;
import com.example.wp.repository.MembershipRepository;
import com.example.wp.repository.WorkspaceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WorkspaceService {

    private final MembershipRepository membershipRepository;
    private final WorkspaceRepository workspaceRepository;
    private final InviteTokenRepository inviteTokenRepository;

    public WorkspaceService(MembershipRepository membershipRepository, WorkspaceRepository workspaceRepository, InviteTokenRepository inviteTokenRepository) {
        this.membershipRepository = membershipRepository;
        this.workspaceRepository = workspaceRepository;
        this.inviteTokenRepository = inviteTokenRepository;
    }

    @Transactional
    public boolean kickUserFromWorkspace(Long workspaceId, Long userId, Long requesterId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        boolean isOwner = workspace.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(requesterId) && m.getRole() == MembershipRole.OWNER);

        if (!isOwner) {
            return false;
        }

        if (requesterId.equals(userId)) {
            return false;
        }

        membershipRepository.deleteByUserAndWorkspace(userId, workspaceId);
        return true;
    }

    @Transactional
    public boolean deleteWorkspace(Long workspaceId, Long requesterId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        boolean isOwner = workspace.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(requesterId) && m.getRole() == MembershipRole.OWNER);

        if (!isOwner) {
            return false;
        }

        inviteTokenRepository.deleteByWorkspace(workspace);

        workspaceRepository.delete(workspace);

        return true;
    }

    public Optional<Workspace> findById(Long workspaceId) {
        return workspaceRepository.findById(workspaceId);
    }

    public void addWorkspace(Workspace workspace) {
        workspaceRepository.save(workspace);
    }

    public Optional<Object> findByName(String name) {
        return workspaceRepository.findByName(name);
    }
}
