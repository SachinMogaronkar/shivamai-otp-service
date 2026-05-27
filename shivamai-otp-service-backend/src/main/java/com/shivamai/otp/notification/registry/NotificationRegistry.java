package com.shivamai.otp.notification.registry;

import com.shivamai.otp.audit.enums.AuditEventType;
import com.shivamai.otp.notification.entity.NotificationMetadata;

import java.util.Map;

public class NotificationRegistry {

    public static final Map<AuditEventType, NotificationMetadata>
            NOTIFICATIONS = Map.ofEntries(

            // =====================================
            // DEVELOPER ACCOUNT EVENTS
            // =====================================

            Map.entry(
                    AuditEventType.DEVELOPER_APPROVED,
                    new NotificationMetadata(
                            "Developer Access Approved - ShivaMai",
                            "email/developer-approved",
                            "/notification/developer-approved"
                    )
            ),

            Map.entry(
                    AuditEventType.DEVELOPER_REJECTED,
                    new NotificationMetadata(
                            "Developer Account Update - ShivaMai",
                            "email/developer-rejected",
                            "/notification/developer-rejected"
                    )
            ),

            Map.entry(
                    AuditEventType.DEVELOPER_SUSPENDED,
                    new NotificationMetadata(
                            "Developer Account Suspended - ShivaMai",
                            "email/developer-suspended",
                            "/notification/developer-suspended"
                    )
            ),

            Map.entry(
                    AuditEventType.DEVELOPER_REACTIVATED,
                    new NotificationMetadata(
                            "Developer Account Reactivated - ShivaMai",
                            "email/developer-reactivated",
                            "/notification/developer-reactivated"
                    )
            ),

            Map.entry(
                    AuditEventType.DEVELOPER_REVOKED_BY_ADMIN,
                    new NotificationMetadata(
                            "Developer Access Revoked",
                            "email/developer-revoked",
                            "/notification/developer-revoked"
                    )
            ),

            Map.entry(
                    AuditEventType.DEVELOPER_WELCOME,
                    new NotificationMetadata(
                            "Welcome to ShivaMai",
                            "email/developer-welcome",
                            "/notification/developer-welcome"
                    )
            ),

            // =====================================
            // APPLICATION EVENTS
            // =====================================

            Map.entry(
                    AuditEventType.APP_CREATED,
                    new NotificationMetadata(
                            "Application Registered Successfully",
                            "email/application-created",
                            "/notification/application-created"
                    )
            ),

            Map.entry(
                    AuditEventType.APP_DISABLED_BY_DEVELOPER,
                    new NotificationMetadata(
                            "Application Disabled",
                            "email/application-disabled",
                            "/notification/application-disabled"
                    )
            ),

            Map.entry(
                    AuditEventType.APP_ENABLED_BY_DEVELOPER,
                    new NotificationMetadata(
                            "Application Enabled",
                            "email/application-enabled",
                            "/notification/application-enabled"
                    )
            ),

            Map.entry(
                    AuditEventType.APP_SUSPENDED,
                    new NotificationMetadata(
                            "Application Suspended",
                            "email/application-suspended",
                            "/notification/application-suspended"
                    )
            ),

            Map.entry(
                    AuditEventType.APP_REACTIVATED,
                    new NotificationMetadata(
                            "Application Reactivated",
                            "email/application-reactivated",
                            "/notification/application-reactivated"
                    )
            ),

            Map.entry(
                    AuditEventType.APP_REVOKED_BY_ADMIN,
                    new NotificationMetadata(
                            "Application Access Revoked",
                            "email/application-revoked",
                            "/notification/application-revoked"
                    )
            ),

            Map.entry(
                    AuditEventType.APP_DELETED_BY_DEVELOPER,
                    new NotificationMetadata(
                            "Application Deleted Permanently",
                            "email/application-deleted",
                            "/notification/application-deleted"
                    )
            ),

            // =====================================
            // APPLICATION CREDENTIAL EVENTS
            // =====================================

            Map.entry(
                    AuditEventType.APPLICATION_SECRET_GENERATED,
                    new NotificationMetadata(
                            "Application Secret Generated",
                            "email/application-secret-generated",
                            "/notification/application-secret-generated"
                    )
            ),

            Map.entry(
                    AuditEventType.APPLICATION_SECRET_ROTATED,
                    new NotificationMetadata(
                            "Application Secret Rotated",
                            "email/application-secret-rotated",
                            "/notification/application-secret-rotated"
                    )
            ),

            // =====================================
            // OTP EMAIL EVENTS
            // =====================================

            Map.entry(
                    AuditEventType.APPLICATION_OTP_SENT,
                    new NotificationMetadata(
                            "Your Verification Code",
                            "email/application-otp",
                            "/notification/application-otp"
                    )
            ),

            Map.entry(
                    AuditEventType.DEVELOPER_LOGIN_OTP_SENT,
                    new NotificationMetadata(
                            "Developer Login Verification",
                            "email/developer-login-otp",
                            "/notification/developer-login-otp"
                    )
            ),

            Map.entry(
                    AuditEventType.DEVELOPER_REGISTRATION_OTP_SENT,
                    new NotificationMetadata(
                            "Developer Registration Verification",
                            "email/developer-registration-otp",
                            "/notification/developer-registration-otp"
                    )
            ),

            Map.entry(
                    AuditEventType.ADMIN_LOGIN_OTP_SENT,
                    new NotificationMetadata(
                            "Administrator Login Verification",
                            "email/admin-login-otp",
                            "/notification/admin-login-otp"
                    )
            )
    );

    private NotificationRegistry() {
    }
}