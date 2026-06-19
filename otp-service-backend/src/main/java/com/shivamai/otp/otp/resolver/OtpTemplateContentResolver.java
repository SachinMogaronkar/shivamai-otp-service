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

            case LOGIN ->
                    OtpTemplateContent.builder()
                            .subject(
                                    applicationName + " Sign In Code"
                            )
                            .template(
                                    "email/admin-login-otp"
                            )
                            .title(
                                    "Complete your sign in"
                            )
                            .subtitle(
                                    "Use the verification code below to securely sign in."
                            )
                            .build();

            case REGISTRATION ->

                    OtpTemplateContent.builder()
                            .subject(
                                    "Verify your email for " + applicationName
                            )
                            .template(
                                    "email/developer-registration-otp"
                            )
                            .title(
                                    "Verify your email"
                            )
                            .subtitle(
                                    "Use the verification code below to complete your registration for "
                                            + applicationName + "."
                            )
                            .build();

            case PASSWORD_RESET ->

                    OtpTemplateContent.builder()
                            .subject(
                                    applicationName + " Password Reset"
                            )
                            .title(
                                    "Reset your password"
                            )
                            .subtitle(
                                    "Use the verification code below to continue resetting your password."
                            )
                            .build();

            case MFA ->

                    OtpTemplateContent.builder()
                            .subject(
                                    applicationName + " Verification Code"
                            )
                            .title(
                                    "Confirm your identity"
                            )
                            .subtitle(
                                    "Use the verification code below to continue securely."
                            )
                            .build();

            default ->

                    OtpTemplateContent.builder()
                            .subject(
                                    applicationName + " Verification Code"
                            )
                            .template(
                                    "email/application-otp"
                            )
                            .title(
                                    "Verification required"
                            )
                            .subtitle(
                                    "Use the verification code below to continue."
                            )
                            .build();
        };
    }
}