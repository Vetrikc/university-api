package com.un.authservice.repository;

import com.un.authservice.entity.UserRole;
import com.un.authservice.entity.UserRoleId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @EntityGraph(attributePaths = {"role"})
    List<UserRole> findByUser_Id(UUID uuid);


    boolean existsByUser_IdAndRole_Id(UUID userId, Long roleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserRole ur where ur.user.id = :userId and ur.role.id = :roleId ")
    int revokeRole(@Param("userId") UUID userId, @Param("roleId") Long roleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
    int revokeAllByUserId(@Param("userId") UUID userId);

    @Query("select ur from UserRole ur JOIN FETCH ur.user where ur.role.name = :roleName")
    List<UserRole> findUserRolesByRoleName(@Param("roleName") String roleName);

    List<UserRole> findAllByUser_Id(UUID userId);

    /**
     * Все роли юзера.
     * Возвращает UserRole — у каждого уже есть role.
     */
    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.user u WHERE u.email = :email")
    List<UserRole> findByUserEmailWithRoles(@Param("email") String email);
}
