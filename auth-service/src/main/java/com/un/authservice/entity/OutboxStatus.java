package com.un.authservice.entity;

public enum OutboxStatus {
    PENDING,      // ждёт отправки
    PROCESSING,   // воркер взял в работу (защита от двух воркеров)
    PUBLISHED,    // успешно отправлено
    FAILED        // ошибка, будет retry по next_attempt_at
}