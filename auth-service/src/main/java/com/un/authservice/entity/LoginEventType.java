package com.un.authservice.entity;

public enum LoginEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    TOKEN_REFRESH,
    ACCOUNT_LOCKED,       // сработала блокировка после N неудач
    ACCOUNT_UNLOCKED
}