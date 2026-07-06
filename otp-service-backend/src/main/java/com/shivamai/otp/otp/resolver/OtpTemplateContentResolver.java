package com.shivamai.otp.otp.resolver;

import com.shivamai.otp.otp.dto.OtpTemplateContent;
import com.shivamai.otp.otp.enums.OtpPurpose;

import org.springframework.stereotype.Component;

@Component
public class OtpTemplateContentResolver {

    public OtpTemplateContent resolve(
            OtpPurpose purpose,
            String applicationName
    ) {

        return switch (purpose) {

            case DEVELOPER_LOGIN -> OtpTemplateContent.builder()
                            .subject("Developer Login Verification")
                            .template("email/developer-login-otp")
                            .title("Verify developer login")
                            .subtitle("Secure authentication verification is required before accessing your developer account.")
                            .build();

            case ADMIN_LOGIN -> OtpTemplateContent.builder()
                            .subject("Administrator Login Verification")
                            .template("email/admin-login-otp")
                            .title("Verify administrator access")
                            .subtitle("A privileged authentication request was initiated for an administrative environment.")
                            .build();

            case REGISTRATION -> OtpTemplateContent.builder()
                            .subject("Verify your email for " + applicationName)
                            .template("email/developer-registration-otp")
                            .title("Verify your email")
                            .subtitle("Use the verification code below to complete your registration.")
                            .build();

            case PASSWORD_RESET -> OtpTemplateContent.builder()
                    .subject(applicationName + " Password Reset")
                    .template("email/password-reset-otp")
                    .title("Reset your password")
                    .subtitle("Use the verification code below to continue resetting your password.")
                    .build();

            case MFA -> OtpTemplateContent.builder()
                    .subject(applicationName + " Verification Code")
                    .template("email/mfa-otp")
                    .title("Confirm your identity")
                    .subtitle("Use the verification code below to continue securely.")
                    .build();

            default -> OtpTemplateContent.builder()
                            .subject(applicationName + " Verification Code")
                            .template("email/application-otp")
                            .title("Verification required")
                            .subtitle("Use the verification code below to continue.")
                            .build();
        };
    }
}