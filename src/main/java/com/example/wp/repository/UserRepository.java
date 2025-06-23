package com.example.wp.repository;

import com.example.wp.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    @Query("""
    select u from UserEntity u
    where u not in (
        select m.user from Membership m where m.workspace.id = :workspaceId
    )""")
    List<UserEntity> findUsersNotInWorkspace(@Param("workspaceId") Long workspaceId);
}
