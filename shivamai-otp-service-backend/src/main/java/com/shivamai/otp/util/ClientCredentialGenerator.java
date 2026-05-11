package com.shivamai.otp.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class ClientCredentialGenerator {

    private static final SecureRandom random = new SecureRandom();

    public static String generateClientId() {
        return "shivamai_" + UUID.randomUUID().toString().substring(0,8);
    }

    public static String generateClientSecret() {

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}