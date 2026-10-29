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
        name = "password_reset_tokens",
        indexes = {
                @Index(name = "idx_prt_user_id",    columnList = "user_id"),
                @Index(name = "idx_prt_token_hash", columnList = "token_hash", unique = true),
                @Index(name = "idx_prt_expires_at", columnList = "expires_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** Чей токен. LAZY — не тянем пользователя без надобности. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_prt_user"))
    private User user;

    /** SHA-256 хеш токена, hex-строка. Сам токен не хранится. */
    @NotBlank
    @Size(max = 255)
    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    /** Когда истекает. TTL — 15–30 минут. */
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Использован ли токен. Одноразовый. */
    @NotNull
    @Column(name = "used", nullable = false)
    private boolean used = false;

    /** Когда использован. null — не использован. */
    @Column(name = "used_at")
    private Instant usedAt;

    /** Когда создан. Заполняется автоматически. */
    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PasswordResetToken() {}

    public PasswordResetToken(User user, String tokenHash, Instant expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    // --- бизнес-методы ---

    public boolean isValid() {
        return !used && expiresAt.isAfter(Instant.now());
    }

    public void markUsed() {
        this.used = true;
        this.usedAt = Instant.now();
    }

    /** Пометить как недействительный без отметки «использован» (для инвалидации при повторном запросе). */
    public void invalidate() {
        this.used = true;
        // usedAt не ставим — токен не использован, а заменён
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PasswordResetToken other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}