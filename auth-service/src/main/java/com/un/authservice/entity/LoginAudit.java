package com.un.authservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "login_audit",
        indexes = {
                @Index(name = "idx_login_audit_user_created",
                        columnList = "user_id, created_at"),
                @Index(name = "idx_login_audit_email_created",
                        columnList = "email, created_at"),
                @Index(name = "idx_login_audit_ip_created",
                        columnList = "ip_address, created_at"),
                @Index(name = "idx_login_audit_created",
                        columnList = "created_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class LoginAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * Ссылка на users.id.
     * NULL — если пользователь не найден (защита от enumeration).
     * LAZY, но обычно сюда не ходят — читают только user_id.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_login_audit_user"))
    private User user;

    /**
     * Email из попытки входа.
     * Храним ВСЕГДА, даже если user_id = NULL —
     * это позволяет видеть перебор по несуществующим аккаунтам.
     */
    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private LoginEventType eventType;

    /** Причина — только для FAILURE / LOCKED. */
    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason", length = 30)
    private LoginFailureReason failureReason;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 500)
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Size(max = 255)
    @Column(name = "device_id", length = 255)
    private String deviceId;

    /** Когда произошло. Заполняется автоматически. */
    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LoginAudit() {}

    private LoginAudit(User user, String email, LoginEventType eventType,
                       LoginFailureReason failureReason) {
        this.user = user;
        this.email = email;
        this.eventType = eventType;
        this.failureReason = failureReason;
    }

    // --- фабричные методы: запрещаем создавать «сырые» записи ---

    public static LoginAudit success(User user, String email) {
        return new LoginAudit(user, email, LoginEventType.LOGIN_SUCCESS, null);
    }

    public static LoginAudit failure(User user, String email, LoginFailureReason reason) {
        return new LoginAudit(user, email, LoginEventType.LOGIN_FAILURE, reason);
    }

    public static LoginAudit locked(User user, String email) {
        return new LoginAudit(user, email, LoginEventType.ACCOUNT_LOCKED,
                LoginFailureReason.ACCOUNT_LOCKED);
    }

    public static LoginAudit logout(User user, String email) {
        return new LoginAudit(user, email, LoginEventType.LOGOUT, null);
    }

    // --- обогащение контекстом (fluent) ---

    public LoginAudit withContext(String ip, String userAgent, String deviceId) {
        this.ipAddress = ip;
        this.userAgent = userAgent;
        this.deviceId = deviceId;
        return this;
    }

    // --- getters only, без сеттеров (append-only) ---

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public UUID getUserId() { return user != null ? user.getId() : null; }
    public String getEmail() { return email; }
    public LoginEventType getEventType() { return eventType; }
    public LoginFailureReason getFailureReason() { return failureReason; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public String getDeviceId() { return deviceId; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoginAudit other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}