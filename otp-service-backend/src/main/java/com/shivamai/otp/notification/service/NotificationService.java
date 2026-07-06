package com.shivamai.otp.notification.service;

import com.shivamai.otp.account.entity.DeveloperAccount;
import com.shivamai.otp.otp.enums.OtpPurpose;

public interface NotificationService {

    // =====================================
    // DEVELOPER STATUS NOTIFICATIONS
    // =====================================

    void sendDeveloperApprovedNotification(
            DeveloperAccount developer
    );

    void sendDeveloperRejectedNotification(
            DeveloperAccount developer
    );

    void sendDeveloperSuspendedNotification(
            DeveloperAccount developer
    );

    void sendDeveloperReactivatedNotification(
            DeveloperAccount developer
    );

    void sendDeveloperRevokedNotification(
            DeveloperAccount developer
    );

    void sendDeveloperWelcomeNotification(
            DeveloperAccount developer
    );

    // =====================================
    // APPLICATION NOTIFICATIONS
    // =====================================

    void sendApplicationCreatedNotification(
            String email,
            String applicationName
    );

    void sendApplicationDisabledNotification(
            String email,
            String applicationName
    );

    void sendApplicationEnabledNotification(
            String email,
            String applicationName
    );

    void sendApplicationReactivatedNotification(
            String email,
            String applicationName
    );

    void sendApplicationSuspendedNotification(
            String email,
            String applicationName
    );

    void sendApplicationRevokedNotification(
            String email,
            String applicationName
    );

    void sendApplicationDeletedNotification(
            String email,
            String applicationName
    );

    // =====================================
    // SECRET NOTIFICATIONS
    // =====================================

    void sendApplicationSecretGeneratedNotification(
            String email,
            String applicationName
    );

    void sendApplicationSecretRotatedNotification(
            String email,
            String applicationName
    );

    // =====================================
    // OTP EMAIL NOTIFICATIONS
    // =====================================

    void sendApplicationOtp(
            String email,
            String displayName,
            String applicationName,
            String otp,
            OtpPurpose purpose,
            int expiryMinutes
    );

    void sendDeveloperLoginOtp(
            String email,
            String fullName,
            String otp,
            int expiryMinutes
    );

    void sendDeveloperRegistrationOtp(
            String email,
            String fullName,
            String otp,
            int expiryMinutes
    );

    void sendAdminLoginOtp(
            String email,
            String fullName,
            String otp,
            int expiryMinutes
    );
}