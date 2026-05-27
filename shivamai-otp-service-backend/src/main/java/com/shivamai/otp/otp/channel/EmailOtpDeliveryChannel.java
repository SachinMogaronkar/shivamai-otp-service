package com.shivamai.otp.otp.channel;

import com.shivamai.otp.notification.dto.EmailRequest;
import com.shivamai.otp.notification.service.EmailService;

import com.shivamai.otp.otp.dto.OtpDeliveryContext;
import com.shivamai.otp.otp.enums.OtpPurpose;
import com.shivamai.otp.otp.enums.OtpType;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import org.thymeleaf.context.Context;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailOtpDeliveryChannel
        implements OtpDeliveryChannel {

    private final EmailService emailService;

    @Override
    public boolean send(
            OtpDeliveryContext deliveryContext
    ) {

        try {

            String email =
                    deliveryContext.getIdentifier();

            String otp =
                    deliveryContext.getOtp();

            OtpType type =
                    deliveryContext.getOtpType();

            String applicationName =
                    deliveryContext.getApplicationName();

            String displayName =
                    deliveryContext.getDisplayName();

            OtpPurpose purpose =
                    deliveryContext.getPurpose();

            int expirySeconds =
                    deliveryContext.getExpirySeconds();

            Context context =
                    new Context();

            context.setVariable(
                    "otp",
                    otp
            );

            context.setVariable(
                    "expiryMinutes",
                    expirySeconds / 60
            );

            context.setVariable(
                    "displayName",
                    displayName
            );

            context.setVariable(
                    "applicationName",
                    applicationName
            );

            context.setVariable(
                    "purpose",
                    purpose != null
                            ? formatPurpose(
                            purpose
                    )
                            : "Verification"
            );

            if (type == OtpType.APPLICATION) {

                if (applicationName == null
                        || applicationName.isBlank()) {

                    throw new IllegalArgumentException(
                            "Application name required for APPLICATION OTP"
                    );
                }
            }

            EmailTemplateMetadata metadata =
                    resolveTemplateMetadata(
                            type,
                            purpose,
                            applicationName
                    );

            EmailRequest request =
                    EmailRequest.builder()
                            .to(email)
                            .subject(
                                    metadata.getSubject()
                            )
                            .template(
                                    metadata.getTemplate()
                            )
                            .context(context)
                            .build();

            emailService.send(
                    request
            );

            log.info(
                    "OTP email delivered successfully to={}",
                    email
            );

            return true;

        } catch (Exception e) {

            log.error(
                    "Failed to deliver OTP email",
                    e
            );

            return false;
        }
    }

    private EmailTemplateMetadata resolveTemplateMetadata(
            OtpType type,
            OtpPurpose purpose,
            String applicationName
    ) {

        switch (type) {

            case SYSTEM -> {

                return EmailTemplateMetadata.builder()
                        .template(
                                "email/system-otp"
                        )
                        .subject(
                                "System Verification Code"
                        )
                        .build();
            }

            case APPLICATION -> {

                String subject =
                        buildApplicationSubject(
                                purpose,
                                applicationName
                        );

                return EmailTemplateMetadata.builder()
                        .template(
                                "email/application-otp"
                        )
                        .subject(
                                subject
                        )
                        .build();
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported OTP type"
            );
        }
    }

    private String buildApplicationSubject(
            OtpPurpose purpose,
            String applicationName
    ) {

        if (applicationName == null
                || applicationName.isBlank()) {

            applicationName =
                    "Application";
        }

        if (purpose == null) {

            return applicationName
                    + " Verification Code";
        }

        return switch (purpose) {

            case LOGIN ->
                    "Login Code for "
                            + applicationName;

            case REGISTRATION ->
                    "Verify Your Email for "
                            + applicationName;

            case PASSWORD_RESET ->
                    "Reset Your Password for "
                            + applicationName;

            case EMAIL_VERIFICATION ->
                    "Verify Your Email for "
                            + applicationName;

            case MFA ->
                    "Multi-Factor Authentication Code";

            case ACCOUNT_RECOVERY ->
                    "Account Recovery Verification";

            case ACCOUNT_DELETION ->
                    "Confirm Account Deletion";

            case TRANSACTION_AUTHORIZATION ->
                    "Transaction Verification Code";
        };
    }

    private String formatPurpose(
            OtpPurpose purpose
    ) {

        return switch (purpose) {

            case LOGIN ->
                    "Login";

            case REGISTRATION ->
                    "Registration";

            case EMAIL_VERIFICATION ->
                    "Email Verification";

            case PASSWORD_RESET ->
                    "Password Reset";

            case MFA ->
                    "MFA Verification";

            case ACCOUNT_RECOVERY ->
                    "Account Recovery";

            case ACCOUNT_DELETION ->
                    "Delete Account";

            case TRANSACTION_AUTHORIZATION ->
                    "Transaction Verification";
        };
    }

    @Override
    public String getChannelName() {

        return "EMAIL";
    }

    @Override
    public void ping() {

        if (emailService == null) {

            throw new RuntimeException(
                    "Email channel unavailable"
            );
        }
    }

    @Getter
    @Builder
    private static class EmailTemplateMetadata {

        private String template;

        private String subject;
    }
}