package com.shivamai.otp.webhook.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;

import java.util.Base64;

public class WebhookSignatureUtil {

    private static final String HMAC_SHA256 =
            "HmacSHA256";

    public static String generateSignature(
            String secret,
            String payload,
            String timestamp
    ) {

        try {

            String data = timestamp + "." + payload;

            Mac mac = Mac.getInstance(HMAC_SHA256);

            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(
                            secret.getBytes(
                                    StandardCharsets.UTF_8
                            ),
                            HMAC_SHA256
                    );

            mac.init(secretKeySpec);

            byte[] hash =
                    mac.doFinal(
                            data.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return Base64.getEncoder()
                    .encodeToString(hash);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate webhook signature",
                    e
            );
        }
    }

    private WebhookSignatureUtil() {
    }
}