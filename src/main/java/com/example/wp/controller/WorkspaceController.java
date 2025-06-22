package com.example.wp.controller;

import com.example.wp.model.*;
import com.example.wp.repository.MembershipRepository;
import com.example.wp.repository.UserRepository;
import com.example.wp.repository.WorkspaceRepository;
import com.example.wp.service.InviteTokenService;
import com.example.wp.service.UserServiceImpl;
import com.example.wp.service.WorkspaceService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

@Controller
@RequestMapping("/workspace")
public class WorkspaceController {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserServiceImpl userService;
    private final InviteTokenService inviteTokenService;

    private final WorkspaceService workspaceService;
    private final MembershipRepository membershipRepository;


    public WorkspaceController(UserRepository userRepository, WorkspaceRepository workspaceRepository, UserServiceImpl userService, InviteTokenService inviteTokenService, WorkspaceService workspaceService, MembershipRepository membershipRepository) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.userService = userService;
        this.inviteTokenService = inviteTokenService;
        this.workspaceService = workspaceService;
        this.membershipRepository = membershipRepository;
    }


    @GetMapping("/all")
    public String showAllWorkspaces(Model model, Authentication authentication){
        UserEntity user = (UserEntity) authentication.getPrincipal();

        List<Workspace> workspaceList = userService.getWorkspacesForUser(user);
        model.addAttribute("workspaceList", workspaceList);

        return "All-Workspaces";
    }

    @GetMapping("/form")
    public String showWorkspaceForm(){
        return "workspace-form";
    }


    @PostMapping("/create")
    public String createWorkspace(@RequestParam String name, Principal principal) {
        if(workspaceRepository.findByName(name).isPresent()){
            return "workspace-form";
        }


        UserEntity user = userRepository.findByUsername(principal.getName()).orElseThrow();


        Workspace workspace = new Workspace();
        workspace.setName(name);
        workspaceRepository.save(workspace);

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setWorkspace(workspace);
        membership.setRole(MembershipRole.OWNER);

        user.getMemberships().add(membership);
        workspace.getMemberships().add(membership);

        membershipRepository.save(membership);

        return "redirect:/workspace/all";
    }

    @GetMapping("/{id}")
    public String viewWorkspace(@PathVariable Long id, Authentication authentication, Model model){
        UserEntity user = (UserEntity) authentication.getPrincipal();

        Workspace workspace = workspaceRepository.findById(id).orElseThrow();
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
        Workspace workspace = workspaceRepository.findById(id)
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

        InviteToken inviteToken = inviteTokenService.getValidTokenForWorkspace(id).orElse(null);


        model.addAttribute("workspace", workspace);
        model.addAttribute("members", members);
        model.addAttribute("inviteToken", inviteToken);

        return "workspace-admin-page";
    }


    @PostMapping("/{id}/generate-invite")
    public String generateInviteToken(@PathVariable Long id, Authentication authentication) {
        Workspace workspace = workspaceRepository.findById(id)
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


        workspaceRepository.save(workspace);

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

}
