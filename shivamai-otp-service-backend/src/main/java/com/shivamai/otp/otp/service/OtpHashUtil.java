package com.shivamai.otp.otp.service;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;

public final class OtpHashUtil {

    private OtpHashUtil() {
    }

    public static String hash(
            String otp
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes =
                    digest.digest(
                            otp.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder hex =
                    new StringBuilder();

            for (byte b : hashBytes) {

                hex.append(
                        String.format("%02x", b)
                );
            }

            return hex.toString();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to hash OTP",
                    e
            );
        }
    }
}