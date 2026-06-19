package com.shivamai.otp.otp.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpCooldownService {

    private static final long COOLDOWN_SECONDS = 30;

    private final ConcurrentHashMap<String, Instant> lastSentMap =
            new ConcurrentHashMap<>();

    public boolean canSendEmail(String identifier) {

        Instant lastSent = lastSentMap.get(identifier);

        if (lastSent == null) {
            return true;
        }

        return Instant.now()
                .isAfter(lastSent.plusSeconds(COOLDOWN_SECONDS));
    }

    public void markEmailSent(String identifier) {
        lastSentMap.put(identifier, Instant.now());
    }
}