package com.example.wp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Membership {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MembershipRole role;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = true)
    private Workspace workspace;

    public Workspace getWorkspace(){
        return this.workspace;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }


    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void setRole(MembershipRole membershipRole) {
        this.role = membershipRole;
    }

    public UserEntity getUser() {
        return this.user;
    }

    public MembershipRole getRole() {
        return this.role;
    }


}
