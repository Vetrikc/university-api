package com.un.authservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Table(
        name = "user_roles",
        indexes = {
                @Index(name = "idx_user_roles_role_id", columnList = "role_id"),
                @Index(name = "idx_user_roles_granted_by", columnList = "granted_by")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class UserRole {
    @EmbeddedId
    private UserRoleId id;

    @NotNull
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_roles_user"))
    private User user;

    @NotNull
    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_roles_role"))
    private Role role;


    @NotNull
    @CreatedDate
    @Column(name = "granted_at", nullable = false, updatable = false)
    private Instant grantedAt;


    @Column(name = "granted_by")
    private UUID grantedBy;

    public UserRole(User user, Role role, UUID grantedBy) {
        this.user = user;
        this.role = role;
        this.id = new UserRoleId(user.getId(), role.getId());
        this.grantedBy = grantedBy;
    }
}
