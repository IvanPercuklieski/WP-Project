package com.example.wp.repository;

import com.example.wp.model.Membership;
import com.example.wp.model.UserEntity;
import com.example.wp.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByUserAndWorkspace(UserEntity user, Workspace workspace);
    Optional<Membership> findByUser_IdAndWorkspace_Id(Long userId, Long workspaceId);
    List<Membership> findByWorkspace_Id(Long workspaceId);

    @Modifying
    @Query("DELETE FROM Membership m WHERE m.user.id = :userId AND m.workspace.id = :workspaceId")
    void deleteByUserAndWorkspace(@Param("userId") Long userId,
                                  @Param("workspaceId") Long workspaceId);

    @Modifying
    @Query("delete from Membership m where m.workspace.id = :workspaceId")
    void deleteByWorkspace(@Param("workspaceId") Long workspaceId);

    boolean existsByWorkspaceAndUser(Workspace workspace, UserEntity user);

    default void addMembership(Workspace workspace, UserEntity user) {
        Membership membership = new Membership();
        membership.setWorkspace(workspace);
        membership.setUser(user);
        save(membership);
    }
}
