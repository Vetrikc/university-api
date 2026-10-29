package com.un.authservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
                @Index(name = "idx_refresh_tokens_token_hash", columnList = "token_hash", unique = true),
                @Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at"),
                @Index(name = "idx_refresh_tokens_replaced_by", columnList = "replaced_by")
        }


)
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Чей токен. LAZY — не тянем пользователя без надобности.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_refresh_tokens_user"))
    private User user;

    /**
     * SHA-256 хеш токена, hex-строка (64 символа). Сам токен не хранится.
     */
    @NotBlank
    @Size(max = 255)
    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    /**
     * Идентификатор устройства (браузер + ОС). Для «активных сессий».
     */
    @Size(max = 255)
    @Column(length = 255)
    private String deviceId;

    /**
     * Полная строка User-Agent. Для аудита.
     */
    @Size(max = 500)
    @Column(length = 500)
    private String userAgent;

    /**
     * IPv4 (15) / IPv6 (45).
     */
    @Size(max = 45)
    @Column(length = 45)
    private String ipAddress;

    /**
     * Когда истекает. После — невалиден.
     */
    @NotNull
    @Column(nullable = false)
    private Instant expiresAt;

    /**
     * Отозван ли токен.
     */
    @NotNull
    @Column(nullable = false)
    private boolean revoked = false;

    /**
     * Когда отозван. null — не отозван.
     */
    @Column()
    private Instant revokedAt;

    /**
     * Новый токен, если была ротация. Self-reference.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_refresh_tokens_replaced_by"))
    private RefreshToken replacedBy;

    /**
     * Когда выдан. Заполняется автоматически.
     */
    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // --- бизнес-методы ---

    public boolean isValid() {
        return !revoked && expiresAt.isAfter(Instant.now());
    }

    public void revoke() {
        this.revoked = true;
        this.revokedAt = Instant.now();
    }

    public void revokeAndReplaceWith(RefreshToken newToken) {
        revoke();
        this.replacedBy = newToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefreshToken that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }


}