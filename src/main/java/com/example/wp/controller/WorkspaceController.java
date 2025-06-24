package com.example.wp.controller;

import com.example.wp.model.*;
import com.example.wp.repository.MembershipRepository;
import com.example.wp.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/workspace")
public class WorkspaceController {
    private final UserServiceImpl userService;
    private final InviteTokenService inviteTokenService;
    private final WorkspaceService workspaceService;
    private final MembershipService membershipService;
    private final InviteService inviteService;


    public WorkspaceController(UserServiceImpl userService, InviteTokenService inviteTokenService, WorkspaceService workspaceService, MembershipService membershipService, InviteService inviteService) {
        this.userService = userService;
        this.inviteTokenService = inviteTokenService;
        this.workspaceService = workspaceService;
        this.membershipService = membershipService;
        this.inviteService = inviteService;
    }


    @GetMapping("/all")
    public String showAllWorkspaces(Model model, Authentication authentication){
        UserEntity user = (UserEntity) authentication.getPrincipal();

        List<Workspace> workspaceList = userService.getWorkspacesForUser(user);
        List<Invitation> invitations = inviteService.findPendingInvationsByUser(user.getId());
        model.addAttribute("workspaceList", workspaceList);
        model.addAttribute("invites", invitations);

        return "All-Workspaces";
    }

    @GetMapping("/form")
    public String showWorkspaceForm(){
        return "workspace-form";
    }


    @PostMapping("/create")
    public String createWorkspace(@RequestParam String name, Principal principal) {
        if(workspaceService.findByName(name).isPresent()){
            return "workspace-form";
        }


        UserEntity user = userService.findByUsername(principal.getName()).orElseThrow();


        Workspace workspace = new Workspace();
        workspace.setName(name);
        workspaceService.addWorkspace(workspace);

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setWorkspace(workspace);
        membership.setRole(MembershipRole.OWNER);

        user.getMemberships().add(membership);
        workspace.getMemberships().add(membership);

        membershipService.addMembership(membership);

        return "redirect:/workspace/all";
    }

    @GetMapping("/{id}")
    public String viewWorkspace(@PathVariable Long id, Authentication authentication, Model model){
        UserEntity user = (UserEntity) authentication.getPrincipal();

        Workspace workspace = workspaceService.findById(id).orElseThrow();
        Membership membership = workspace.getMemberships().stream()
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElse(null);

        if (membership == null) {
            return "redirect:/workspace/all";
        }

        model.addAttribute("workspace", workspace);
        model.addAttribute("isOwner", membership.getRole() == MembershipRole.OWNER);

        return "workspace-view";

    }

    @GetMapping("/{id}/admin")
    public String workspaceAdminPage(@PathVariable Long id, Authentication authentication, Model model) {
        Workspace workspace = workspaceService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid workspace ID"));
        UserEntity user = (UserEntity) authentication.getPrincipal();

        boolean isOwner = workspace.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()) && m.getRole() == MembershipRole.OWNER);

        if (!isOwner) {
            return "redirect:/workspace/all";
        }

        List<UserEntity> members = workspace.getMemberships().stream()
                .filter(membership -> membership.getRole() != MembershipRole.OWNER)
                .map(Membership::getUser)
                .toList();

        List<UserEntity> nonMembers = userService.findUsersNotInWorkspace(workspace.getId());

        InviteToken inviteToken = inviteTokenService.getValidTokenForWorkspace(id).orElse(null);

        Set<Long> invitedUserIds = inviteService.findPendingInvitesByWorkspace(workspace.getId())
                .stream()
                .map(invite -> invite.getInvitedUser().getId())
                .collect(Collectors.toSet());

        model.addAttribute("invitedUserIds", invitedUserIds);

        model.addAttribute("workspace", workspace);
        model.addAttribute("members", members);
        model.addAttribute("inviteToken", inviteToken);
        model.addAttribute("nonmembers", nonMembers);
        model.addAttribute("invitedUser", invitedUserIds);

        return "workspace-admin-page";
    }


    @PostMapping("/{id}/generate-invite")
    public String generateInviteToken(@PathVariable Long id, Authentication authentication) {
        Workspace workspace = workspaceService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid workspace ID"));


        UserEntity user = (UserEntity) authentication.getPrincipal();
        boolean isOwner = workspace.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()) && m.getRole() == MembershipRole.OWNER);

        if (!isOwner) {
            return "redirect:/workspace/all";
        }


        InviteToken existingInvite = inviteTokenService.getValidTokenForWorkspace(id).orElse(null);
        if (existingInvite != null) {
            return "redirect:/workspace/" + id + "/admin";
        }

        inviteTokenService.createInviteToken(workspace);

        return "redirect:/workspace/" + id + "/admin";
    }

    @GetMapping("/join/{token}")
    public String joinWorkspace(@PathVariable String token, Authentication authentication, Model model) {

        InviteToken inviteToken = inviteTokenService.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite token"));


        if (inviteToken.getExpiresAt().isBefore(Instant.now())) {
            model.addAttribute("error", "This invite link has expired.");
            return "error-page";
        }

        Workspace workspace = inviteToken.getWorkspace();

        UserEntity user = (UserEntity) authentication.getPrincipal();

        if (workspace.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()))) {
            model.addAttribute("error", "You are already a member of this workspace.");
            return "error-page";
        }


        Membership membership = new Membership();
        membership.setUser(user);
        membership.setWorkspace(workspace);
        membership.setRole(MembershipRole.MEMBER);
        workspace.getMemberships().add(membership);

        workspaceService.addWorkspace(workspace);

        inviteService.deletePendingInviteForUserAndWorkspace(user, workspace);

        return "redirect:/workspace/all";
    }

    @PostMapping("/{workspaceId}/kick/{userId}")
    public String kickUserFromWorkspace(
            @PathVariable Long workspaceId,
            @PathVariable Long userId,
            Authentication authentication) {

        UserEntity currentUser = (UserEntity) authentication.getPrincipal();
        boolean success = workspaceService.kickUserFromWorkspace(workspaceId, userId, currentUser.getId());

        if (!success) {
            if (currentUser.getId().equals(userId)) {
                return "redirect:/workspace/" + workspaceId + "/admin";
            } else {
                return "redirect:/workspace/all";
            }
        }

        return "redirect:/workspace/" + workspaceId + "/admin";
    }

    @PostMapping("/{workspaceId}/delete")
    public String deleteWorkspace(
            @PathVariable Long workspaceId,
            Authentication authentication) {

        UserEntity currentUser = (UserEntity) authentication.getPrincipal();
        boolean success = workspaceService.deleteWorkspace(workspaceId, currentUser.getId());

        if (!success) {
            return "redirect:/workspace/all";
        }

        return "redirect:/workspace/all";
    }

    @PostMapping("/{workspaceId}/invite")
    public String inviteUser(@PathVariable Long workspaceId, @RequestParam Long userId, Authentication authentication, RedirectAttributes redirectAttributes){
        Workspace workspace = workspaceService.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid workspace ID"));
        UserEntity inviter = (UserEntity) authentication.getPrincipal();
        UserEntity invitedUser = userService.findById(userId).orElseThrow();

        try {
            inviteService.createInvite(invitedUser, workspace, inviter);
            redirectAttributes.addFlashAttribute("success", "Invitation sent successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/workspace/" + workspaceId + "/admin";
    }

    @PostMapping("/invites/accept")
    public String acceptInvite(@RequestParam Long inviteId){
        inviteService.acceptInvite(inviteId);
        return "redirect:/workspace/all";
    }

    @PostMapping("/invites/decline")
    public String declineInvite(@RequestParam Long inviteId){
        inviteService.declineInvite(inviteId);
        return "redirect:/workspace/all";
    }

}
