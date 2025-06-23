package com.example.wp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UserEntity invitedUser;

    @ManyToOne(optional = false)
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    private InviteStatus status;

    @ManyToOne(optional = false)
    private UserEntity inviter;

    public void setInviter(UserEntity inviter){
        this.inviter = inviter;
    }

    public UserEntity getInviter(){
        return inviter;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getInvitedUser() {
        return invitedUser;
    }

    public void setInvitedUser(UserEntity invitedUser) {
        this.invitedUser = invitedUser;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public InviteStatus getStatus() {
        return status;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }
}
