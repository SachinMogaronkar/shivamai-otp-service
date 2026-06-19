package com.shivamai.otp.otp.channel;

import com.shivamai.otp.otp.dto.OtpDeliveryContext;
import com.shivamai.otp.otp.enums.OtpPurpose;
import com.shivamai.otp.otp.enums.OtpType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsOtpDeliveryChannel
        implements OtpDeliveryChannel {

    @Override
    public boolean send(
            OtpDeliveryContext context
    ) {

        try {

            String phone =
                    context.getIdentifier();

            String otp =
                    context.getOtp();

            OtpType type =
                    context.getOtpType();

            String applicationName =
                    context.getApplicationName();

            OtpPurpose purpose =
                    context.getPurpose();

            String message =
                    buildMessage(
                            otp,
                            type,
                            applicationName,
                            purpose
                    );

            log.info(
                    "SMS sent to={}",
                    phone
            );

            log.info(
                    "SMS content={}",
                    message
            );

            return true;

        } catch (Exception e) {

            log.error(
                    "Failed to deliver SMS OTP",
                    e
            );

            return false;
        }
    }

    private String buildMessage(
            String otp,
            OtpType type,
            String applicationName,
            OtpPurpose purpose
    ) {

        return switch (type) {

            case SYSTEM ->

                    "Your system verification code is: "
                            + otp;

            case APPLICATION -> {

                String appName =
                        applicationName != null
                                ? applicationName
                                : "Application";

                String purposeText =
                        purpose != null
                                ? formatPurpose(
                                purpose
                        )
                                : "Verification";

                yield appName
                        + " "
                        + purposeText
                        + " code: "
                        + otp;
            }
        };
    }

    private String formatPurpose(
            OtpPurpose purpose
    ) {

        return switch (purpose) {

            case LOGIN -> "login";

            case REGISTRATION -> "registration";

            case EMAIL_VERIFICATION -> "email verification";

            case PASSWORD_RESET -> "password reset";

            case MFA -> "MFA verification";

            case ACCOUNT_RECOVERY -> "account recovery";

            case ACCOUNT_DELETION -> "delete account";

            case TRANSACTION_AUTHORIZATION -> "transaction verification";
        };
    }

    @Override
    public String getChannelName() {

        return "SMS";
    }

    @Override
    public void ping() {

        log.debug(
                "SMS channel ping successful"
        );
    }
}