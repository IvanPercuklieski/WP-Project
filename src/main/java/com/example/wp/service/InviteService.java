package com.example.wp.service;

import com.example.wp.model.Invitation;
import com.example.wp.model.InviteStatus;
import com.example.wp.model.UserEntity;
import com.example.wp.model.Workspace;
import com.example.wp.repository.InviteRepository;
import com.example.wp.repository.MembershipRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InviteService {
    private final InviteRepository inviteRepository;
    private final MembershipRepository membershipRepository;


    public InviteService(InviteRepository inviteRepository, MembershipRepository membershipRepository) {
        this.inviteRepository = inviteRepository;
        this.membershipRepository = membershipRepository;
    }

    public List<Invitation> findPendingInvitesByWorkspace(Long workspaceId) {
        return inviteRepository.findByWorkspaceIdAndStatus(workspaceId, InviteStatus.PENDING);
    }

    public List<Invitation> findPendingInvationsByUser(Long id){
        return inviteRepository.findByInvitedUserIdAndStatus(id, InviteStatus.PENDING);
    }

    public Invitation createInvite(UserEntity invitedUser, Workspace workspace, UserEntity inviter){
        if(membershipRepository.existsByWorkspaceAndUser(workspace, invitedUser)){
            throw new IllegalStateException("User is already a member.");
        }

        if(inviteRepository.existsByInvitedUserAndWorkspaceAndStatus(invitedUser, workspace, InviteStatus.PENDING)){
            throw new IllegalStateException("User is already a invited.");
        }

        Invitation invitation = new Invitation();
        invitation.setInvitedUser(invitedUser);
        invitation.setWorkspace(workspace);
        invitation.setStatus(InviteStatus.PENDING);
        invitation.setInviter(inviter);

        return inviteRepository.save(invitation);
    }

    public void acceptInvite(Long invitationId){
        Invitation invitation = inviteRepository.findById(invitationId).orElseThrow();

        UserEntity invitedUser = invitation.getInvitedUser();
        Workspace workspace = invitation.getWorkspace();

        membershipRepository.addMembership(workspace, invitedUser);
        inviteRepository.delete(invitation);
    }

    public void declineInvite(Long invitationId){
        Invitation invitation = inviteRepository.findById(invitationId).orElseThrow();

        inviteRepository.delete(invitation);
    }
}
