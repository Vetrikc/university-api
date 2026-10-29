package com.un.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Embeddable
@EqualsAndHashCode
public class UserRoleId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    protected UserRoleId() {} // для JPA

    public UserRoleId(UUID userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }
}