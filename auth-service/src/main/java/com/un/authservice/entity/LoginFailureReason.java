package com.un.authservice.entity;

public enum LoginFailureReason {
    BAD_CREDENTIALS,      // неверный email/пароль
    ACCOUNT_LOCKED,       // аккаунт заблокирован
    ACCOUNT_DISABLED,     // soft-deleted / отключён
    EMAIL_NOT_VERIFIED,   // если есть верификация
    TOKEN_EXPIRED,        // refresh-токен истёк
    TOKEN_REVOKED         // refresh-токен отозван
}