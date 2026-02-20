package com.shopjoy.entity;

/**
 * Enum representing different types of security audit events.
 */
public enum SecurityEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    REGISTRATION,
    LOGOUT,
    ACCESS_DENIED,
    TOKEN_EXPIRED,
    TOKEN_INVALID,
    PASSWORD_CHANGE,
    OAUTH2_LOGIN_SUCCESS,
    OAUTH2_LOGIN_FAILURE
}
