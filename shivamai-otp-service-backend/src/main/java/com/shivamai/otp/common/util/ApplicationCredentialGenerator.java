package com.shivamai.otp.common.util;

import java.security.SecureRandom;

import java.util.Base64;
import java.util.UUID;

public final class ApplicationCredentialGenerator {

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private ApplicationCredentialGenerator() {
    }

    public static String generateClientId() {

        return "shivamai_"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8);
    }

    public static String generateClientSecret() {

        byte[] bytes = new byte[32];

        RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}