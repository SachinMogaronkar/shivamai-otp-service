package com.shivamai.otp.audit.enums;

public enum AuditEventType {

    // =====================================
    // API ACCESS
    // =====================================

    API_REQUEST_RECEIVED,

    // =====================================
    // APPLICATION CREDENTIALS
    // =====================================

    APPLICATION_SECRET_GENERATED,
    APPLICATION_SECRET_ROTATED,

    // =====================================
    // OTP EVENTS
    // =====================================

    OTP_REQUESTED,
    OTP_REUSED,
    OTP_RESENT,
    OTP_DELIVERED,
    OTP_VERIFIED,
    OTP_INVALID,
    OTP_EXPIRED,
    OTP_BLOCKED,

    OTP_REQUEST_FAILED,
    OTP_RESEND_FAILED,
    OTP_VERIFICATION_FAILED,

    // =====================================
    // OTP DELIVERY TYPES
    // =====================================

    DEVELOPER_LOGIN_OTP_SENT,
    DEVELOPER_REGISTRATION_OTP_SENT,
    ADMIN_LOGIN_OTP_SENT,
    APPLICATION_OTP_SENT,

    // =====================================
    // RATE LIMITING
    // =====================================

    RATE_LIMIT_EXCEEDED,

    // =====================================
    // AUTHENTICATION
    // =====================================

    LOGIN_INITIATED,
    LOGIN_SUCCESS,
    LOGIN_FAILED,

    LOGOUT,

    // =====================================
    // DEVELOPER ACCOUNT LIFECYCLE
    // =====================================

    DEVELOPER_WELCOME,
    DEVELOPER_REGISTERED,
    REGISTRATION_FAILED,

    DEVELOPER_APPROVED,
    DEVELOPER_REJECTED,
    DEVELOPER_SUSPENDED,
    DEVELOPER_REACTIVATED,
    DEVELOPER_REVOKED_BY_ADMIN,

    ACCOUNT_DELETED_BY_DEVELOPER,
    ACCOUNT_DELETION_FAILED,

    // =====================================
    // APPLICATION LIFECYCLE
    // =====================================

    APP_CREATED,
    APP_ACTIVATED,
    APP_REACTIVATED,
    APP_SUSPENDED,
    APP_DELETED_BY_DEVELOPER,
    APP_DISABLED_BY_DEVELOPER,
    APP_ENABLED_BY_DEVELOPER,
    APP_REVOKED_BY_ADMIN,

    // =====================================
    // WEBHOOKS
    // =====================================

    WEBHOOK_DELIVERY_FAILED,

    // =====================================
    // NOTIFICATIONS
    // =====================================

    NOTIFICATION_SENT,
    NOTIFICATION_FAILED
}