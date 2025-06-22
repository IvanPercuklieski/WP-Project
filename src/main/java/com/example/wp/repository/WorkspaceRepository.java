package com.example.wp.repository;

import com.example.wp.model.UserEntity;
import com.example.wp.model.Workspace;
import org.hibernate.jdbc.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Optional<Object> findByName(String name);


    @Query("SELECT w FROM Workspace w JOIN FETCH w.memberships m WHERE m.user = :user")
    List<Workspace> findWorkspacesByUserWithMemberships(UserEntity user);



}
