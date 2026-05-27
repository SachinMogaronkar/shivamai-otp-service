package com.shivamai.otp.common.security;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class JwtBlacklistService {

    private final Map<String, Long> blacklist =
            new ConcurrentHashMap<>();

    public void blacklist(
            String token,
            long expiryTime
    ) {

        if (token == null || token.isBlank()) {

            throw new IllegalArgumentException(
                    "Token cannot be null or blank"
            );
        }

        blacklist.put(
                token,
                expiryTime
        );

        log.info(
                "JWT token blacklisted successfully"
        );
    }

    public boolean isBlacklisted(
            String token
    ) {

        if (token == null || token.isBlank()) {

            return false;
        }

        Long expiry =
                blacklist.get(token);

        if (expiry == null) {

            return false;
        }

        // =====================================
        // AUTO CLEANUP EXPIRED TOKENS
        // =====================================

        if (System.currentTimeMillis() > expiry) {

            blacklist.remove(token);

            log.debug(
                    "Expired blacklisted JWT removed"
            );

            return false;
        }

        log.warn(
                "Blacklisted JWT access attempt detected"
        );

        return true;
    }
}