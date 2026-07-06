package com.shivamai.otp.otp.util;

import com.shivamai.otp.otp.enums.OtpPurpose;

public final class OtpPurposeFormatter {

    private OtpPurposeFormatter() {
    }

    public static String humanize(
            OtpPurpose purpose
    ) {

        return switch (purpose) {

            case DEVELOPER_LOGIN -> "Developer Login";

            case ADMIN_LOGIN -> "Admin Login";

            case REGISTRATION ->
                    "Registration";

            case EMAIL_VERIFICATION ->
                    "Email Verification";

            case PASSWORD_RESET ->
                    "Password Reset";

            case MFA ->
                    "Identity Verification";

            case ACCOUNT_RECOVERY ->
                    "Account Recovery";

            case ACCOUNT_DELETION ->
                    "Account Deletion";

            case TRANSACTION_AUTHORIZATION ->
                    "Transaction Authorization";
        };
    }
}