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
        name = "email_verification_tokens",
        indexes = {
                @Index(name = "idx_evt_user_id",    columnList = "user_id"),
                @Index(name = "idx_evt_token_hash", columnList = "token_hash", unique = true),
                @Index(name = "idx_evt_expires_at", columnList = "expires_at"),
                @Index(name = "idx_evt_email",      columnList = "email")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class EmailVerificationTokens {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** Чей токен. LAZY — не тянем пользователя без надобности. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_evt_user"))
    private User user;

    /**
     * Email, который подтверждается.
     * Хранится отдельно от users.email, потому что при смене email
     * users.email может ещё не совпадать — подтверждаем новый.
     */
    @NotBlank
    @Size(max = 255)
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /** SHA-256 хеш токена, hex-строка. Сам токен не хранится. */
    @NotBlank
    @Size(max = 255)
    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    /** Когда истекает. TTL — обычно 24–48 часов. */
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Подтверждён ли токен. Одноразовый. */
    @NotNull
    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    /** Когда подтверждён. null — не подтверждён. */
    @Column(name = "verified_at")
    private Instant verifiedAt;

    /** Когда создан. Заполняется автоматически. */
    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected EmailVerificationTokens() {}

    public EmailVerificationTokens(User user, String email,
                                   String tokenHash, Instant expiresAt) {
        this.user = user;
        this.email = email;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    // --- бизнес-методы ---

    public boolean isValid() {
        return !verified && expiresAt.isAfter(Instant.now());
    }

    public void markVerified() {
        this.verified = true;
        this.verifiedAt = Instant.now();
    }

    /** Инвалидировать без подтверждения — при повторной отправке. */
    public void invalidate() {
        this.verified = true;
        // verifiedAt не ставим — токен не подтверждён, а заменён
    }

    // --- getters ---

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public UUID getUserId() { return user != null ? user.getId() : null; }
    public String getEmail() { return email; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isVerified() { return verified; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailVerificationTokens other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}