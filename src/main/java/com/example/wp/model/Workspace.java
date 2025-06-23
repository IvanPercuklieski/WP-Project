package com.example.wp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String name;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true,  fetch = FetchType.LAZY)
    private java.util.List<Membership> memberships = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Invitation> invitations = new ArrayList<>();
    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public Long getId(){
        return this.id;
    }

    public List<Membership> getMemberships() {
        return memberships;
    }

}
