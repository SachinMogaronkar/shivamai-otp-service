package com.shivamai.otp.notification.service;

import com.shivamai.otp.audit.dto.request.AuditLogRequest;
import com.shivamai.otp.audit.enums.AuditActorType;
import com.shivamai.otp.audit.enums.AuditEventType;
import com.shivamai.otp.audit.logging.ApiAccessLogger;

import com.shivamai.otp.notification.dto.EmailRequest;
import com.shivamai.otp.notification.entity.NotificationMetadata;
import com.shivamai.otp.notification.registry.NotificationRegistry;

import com.shivamai.otp.otp.dto.OtpTemplateContent;
import com.shivamai.otp.otp.enums.OtpPurpose;
import com.shivamai.otp.otp.resolver.OtpTemplateContentResolver;
import com.shivamai.otp.otp.util.OtpPurposeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl
        implements NotificationService {

    private final EmailService emailService;

    private final ApiAccessLogger apiAccessLogger;

    private final OtpTemplateContentResolver otpTemplateContentResolver;

    // =====================================
    // DEVELOPER STATUS NOTIFICATIONS
    // =====================================

    @Override
    public void sendDeveloperApprovedNotification(
            String email
    ) {

        sendIdentifierTemplate(
                email,
                AuditEventType.DEVELOPER_APPROVED
        );
    }

    @Override
    public void sendDeveloperRejectedNotification(
            String email
    ) {

        sendIdentifierTemplate(
                email,
                AuditEventType.DEVELOPER_REJECTED
        );
    }

    @Override
    public void sendDeveloperSuspendedNotification(
            String email
    ) {

        sendIdentifierTemplate(
                email,
                AuditEventType.DEVELOPER_SUSPENDED
        );
    }

    @Override
    public void sendDeveloperReactivatedNotification(
            String email
    ) {

        sendIdentifierTemplate(
                email,
                AuditEventType.DEVELOPER_REACTIVATED
        );
    }

    @Override
    public void sendDeveloperWelcomeNotification(
            String email
    ) {

        sendIdentifierTemplate(
                email,
                AuditEventType.DEVELOPER_WELCOME
        );
    }

    @Override
    public void sendDeveloperRevokedNotification(
            String email
    ) {

        sendIdentifierTemplate(
                email,
                AuditEventType.DEVELOPER_REVOKED_BY_ADMIN
        );
    }

    // =====================================
    // APPLICATION NOTIFICATIONS
    // =====================================

    @Override
    public void sendApplicationCreatedNotification(
            String email,
            String applicationName
    ) {

        sendApplicationTemplate(
                email,
                applicationName,
                AuditEventType.APP_CREATED
        );
    }

    @Override
    public void sendApplicationSuspendedNotification(
            String email,
            String applicationName
    ) {

        sendApplicationTemplate(
                email,
                applicationName,
                AuditEventType.APP_SUSPENDED
        );
    }

    @Override
    public void sendApplicationDisabledNotification(
            String email,
            String applicationName
    ) {

        sendApplicationTemplate(
                email,
                applicationName,
                AuditEventType.APP_DISABLED_BY_DEVELOPER
        );
    }

    @Override
    public void sendApplicationEnabledNotification(
            String email,
            String applicationName
    ) {

        sendApplicationTemplate(
                email,
                applicationName,
                AuditEventType.APP_ENABLED_BY_DEVELOPER
        );
    }

    @Override
    public void sendApplicationReactivatedNotification(
            String email,
            String applicationName
    ) {

        sendApplicationTemplate(
                email,
                applicationName,
                AuditEventType.APP_REACTIVATED
        );
    }

    @Override
    public void sendApplicationDeletedNotification(
            String email,
            String applicationName
    ) {

        sendApplicationTemplate(
                email,
                applicationName,
                AuditEventType.APP_DELETED_BY_DEVELOPER
        );
    }

    @Override
    public void sendApplicationRevokedNotification(
            String email,
            String applicationName
    ) {

        sendApplicationTemplate(
                email,
                applicationName,
                AuditEventType.APP_REVOKED_BY_ADMIN
        );
    }

    // =====================================
    // SECRET NOTIFICATIONS
    // =====================================

    @Override
    public void sendApplicationSecretGeneratedNotification(
            String email,
            String applicationName
    ) {

        sendSecretTemplate(
                email,
                applicationName,
                AuditEventType.APPLICATION_SECRET_GENERATED
        );
    }

    @Override
    public void sendApplicationSecretRotatedNotification(
            String email,
            String applicationName
    ) {

        sendSecretTemplate(
                email,
                applicationName,
                AuditEventType.APPLICATION_SECRET_ROTATED
        );
    }

    // =====================================
    // OTP EMAILS
    // =====================================

    @Override
    public void sendApplicationOtp(
            String email,
            String displayName,
            String applicationName,
            String otp,
            OtpPurpose purpose,
            int expiryMinutes
    ) {

        OtpTemplateContent content =
                otpTemplateContentResolver.resolve(
                        purpose,
                        applicationName
                );

        NotificationMetadata metadata =
                metadata(
                        AuditEventType.APPLICATION_OTP_SENT
                );

        executeNotification(
                email,
                metadata.endpoint(),
                AuditEventType.APPLICATION_OTP_SENT,
                () -> {

                    Context context =
                            new Context();

                    context.setVariable(
                            "otp",
                            otp
                    );

                    context.setVariable(
                            "expiryMinutes",
                            expiryMinutes
                    );

                    context.setVariable(
                            "applicationName",
                            applicationName
                    );

                    context.setVariable(
                            "displayName",
                            displayName != null
                                    && !displayName.isBlank()
                                    ? displayName
                                    : "User"
                    );

                    context.setVariable(
                            "title",
                            content.getTitle()
                    );

                    context.setVariable(
                            "subtitle",
                            content.getSubtitle()
                    );

                    context.setVariable(
                            "purpose",
                            OtpPurposeFormatter.humanize(
                                    purpose
                            )
                    );

                    context.setVariable(
                            "requestTime",
                            java.time.LocalDateTime.now()
                                    .format(
                                            java.time.format.DateTimeFormatter.ofPattern(
                                                    "dd MMM yyyy • hh:mm a"
                                            )
                                    )
                    );

                    EmailRequest request =
                            EmailRequest.builder()
                                    .to(email)
                                    .subject(
                                            content.getSubject()
                                    )
                                    .template(
                                            metadata.template()
                                    )
                                    .context(context)
                                    .build();

                    emailService.send(
                            request
                    );
                }
        );
    }

    @Override
    public void sendDeveloperLoginOtp(
            String email,
            String otp,
            int expiryMinutes
    ) {

        sendOtpTemplate(
                email,
                otp,
                expiryMinutes,
                AuditEventType.DEVELOPER_LOGIN_OTP_SENT
        );
    }

    @Override
    public void sendDeveloperRegistrationOtp(
            String email,
            String otp,
            int expiryMinutes
    ) {

        sendOtpTemplate(
                email,
                otp,
                expiryMinutes,
                AuditEventType.DEVELOPER_REGISTRATION_OTP_SENT
        );
    }

    @Override
    public void sendAdminLoginOtp(
            String email,
            String otp,
            int expiryMinutes
    ) {

        sendOtpTemplate(
                email,
                otp,
                expiryMinutes,
                AuditEventType.ADMIN_LOGIN_OTP_SENT
        );
    }

    // =====================================
    // EXECUTION
    // =====================================

    private void executeNotification(
            String email,
            String endpoint,
            AuditEventType successEvent,
            Runnable notificationAction
    ) {

        try {

            notificationAction.run();

            apiAccessLogger.logEvent(
                    AuditLogRequest.builder()
                            .clientId("SYSTEM")
                            .identifier(email)
                            .actorType(AuditActorType.SYSTEM)
                            .endpoint(endpoint)
                            .eventType(successEvent)
                            .ip("SYSTEM")
                            .status(200)
                            .build()
            );

            log.info(
                    "Notification processed successfully for={}",
                    email
            );

        } catch (Exception e) {

            apiAccessLogger.logEvent(
                    AuditLogRequest.builder()
                            .clientId("SYSTEM")
                            .identifier(email)
                            .actorType(AuditActorType.SYSTEM)
                            .endpoint(endpoint)
                            .eventType(AuditEventType.NOTIFICATION_FAILED)
                            .ip("SYSTEM")
                            .status(500)
                            .build()
            );

            log.error(
                    "Notification processing failed for={}",
                    email,
                    e
            );
        }
    }

    // =====================================
    // IDENTIFIER TEMPLATE
    // =====================================

    private void sendIdentifierTemplate(
            String email,
            AuditEventType eventType
    ) {

        NotificationMetadata metadata =
                metadata(eventType);

        executeNotification(
                email,
                metadata.endpoint(),
                eventType,
                () -> {

                    Context context =
                            new Context();

                    context.setVariable(
                            "identifier",
                            email
                    );

                    EmailRequest request =
                            EmailRequest.builder()
                                    .to(email)
                                    .subject(metadata.subject())
                                    .template(metadata.template())
                                    .context(context)
                                    .build();

                    emailService.send(request);
                }
        );
    }

    // =====================================
    // APPLICATION TEMPLATE
    // =====================================

    private void sendApplicationTemplate(
            String email,
            String applicationName,
            AuditEventType eventType
    ) {

        NotificationMetadata metadata =
                metadata(eventType);

        executeNotification(
                email,
                metadata.endpoint(),
                eventType,
                () -> {

                    Context context =
                            new Context();

                    context.setVariable(
                            "applicationName",
                            applicationName
                    );

                    EmailRequest request =
                            EmailRequest.builder()
                                    .to(email)
                                    .subject(metadata.subject())
                                    .template(metadata.template())
                                    .context(context)
                                    .build();

                    emailService.send(request);
                }
        );
    }

    // =====================================
    // SECRET TEMPLATE
    // =====================================

    private void sendSecretTemplate(
            String email,
            String applicationName,
            AuditEventType eventType
    ) {

        NotificationMetadata metadata =
                metadata(eventType);

        executeNotification(
                email,
                metadata.endpoint(),
                eventType,
                () -> {

                    Context context =
                            new Context();

                    context.setVariable(
                            "applicationName",
                            applicationName
                    );

                    context.setVariable(
                            "identifier",
                            email
                    );

                    EmailRequest request =
                            EmailRequest.builder()
                                    .to(email)
                                    .subject(metadata.subject())
                                    .template(metadata.template())
                                    .context(context)
                                    .build();

                    emailService.send(request);
                }
        );
    }

    // =====================================
    // OTP TEMPLATE
    // =====================================

    private void sendOtpTemplate(
            String email,
            String otp,
            int expiryMinutes,
            AuditEventType eventType
    ) {

        NotificationMetadata metadata =
                metadata(eventType);

        executeNotification(
                email,
                metadata.endpoint(),
                eventType,
                () -> {

                    Context context =
                            new Context();

                    context.setVariable(
                            "otp",
                            otp
                    );

                    context.setVariable(
                            "expiryMinutes",
                            expiryMinutes
                    );

                    context.setVariable(
                            "identifier",
                            email
                    );

                    EmailRequest request =
                            EmailRequest.builder()
                                    .to(email)
                                    .subject(metadata.subject())
                                    .template(metadata.template())
                                    .context(context)
                                    .build();

                    emailService.send(request);
                }
        );
    }

    // =====================================
    // METADATA
    // =====================================

    private NotificationMetadata metadata(
            AuditEventType eventType
    ) {

        NotificationMetadata metadata =
                NotificationRegistry.NOTIFICATIONS.get(
                        eventType
                );

        if (metadata == null) {

            throw new IllegalArgumentException(
                    "Notification metadata missing for event="
                            + eventType
            );
        }

        return metadata;
    }
}