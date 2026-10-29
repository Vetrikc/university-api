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
        name = "outbox_events",
        indexes = {
                @Index(name = "idx_outbox_status_created",
                        columnList = "status, created_at"),//TODO партиальный индекс сделать  status, created_at
                @Index(name = "idx_outbox_aggregate",
                        columnList = "aggregate_type, aggregate_id"),
                @Index(name = "idx_outbox_next_attempt",
                        columnList = "next_attempt_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class OutboxEvents {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** Тип агрегата — к чему относится событие. Например, "User". */
    @NotBlank
    @Size(max = 100)
    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    /** ID агрегата. UUID в вашем проекте, но тип — строка для универсальности. */
    @NotNull
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    /** Тип события. Например, "UserRegistered", "PasswordResetRequested". */
    @NotBlank
    @Size(max = 150)
    @Column(name = "event_type", nullable = false, length = 150)
    private String eventType;

    /** Тело события — JSON. */
    @NotBlank
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    /** Заголовки для брокера (traceId, correlationId) — опционально, JSON. */
    @Column(name = "headers", columnDefinition = "TEXT")
    private String headers;

    /** Статус обработки. */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status = OutboxStatus.PENDING;

    /** Сколько раз пытались отправить. */
    @NotNull
    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    /** Когда в следующий раз пробовать (для backoff). null — можно сразу. */
    @NotNull
    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt = Instant.EPOCH;

    /** Последняя ошибка. */
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    /** Когда успешно опубликовано. */
    @Column(name = "published_at")
    private Instant publishedAt;

    /** Когда создано. Заполняется автоматически. */
    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected OutboxEvents() {}

    public OutboxEvents(String aggregateType, UUID aggregateId,
                        String eventType, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
    }

    // --- бизнес-методы ---

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.lastError = null;
    }

    public void markFailed(String error, Instant nextAttemptAt) {
        this.status = OutboxStatus.FAILED;
        this.lastError = truncate(error, 4000);
        this.nextAttemptAt = nextAttemptAt;
        this.attempts++;
    }

    public void markProcessing() {
        this.status = OutboxStatus.PROCESSING;
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    // --- getters/setters ---

    public UUID getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public String getHeaders() { return headers; }
    public void setHeaders(String headers) { this.headers = headers; }
    public OutboxStatus getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public String getLastError() { return lastError; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutboxEvents other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}