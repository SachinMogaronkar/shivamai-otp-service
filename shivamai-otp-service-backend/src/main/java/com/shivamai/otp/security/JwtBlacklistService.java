package com.shivamai.otp.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, long expiryTime) {
        blacklist.put(token, expiryTime);
    }

    public boolean isBlacklisted(String token) {

        Long expiry = blacklist.get(token);

        if (expiry == null) {
            return false;
        }

        // remove expired tokens automatically
        if (System.currentTimeMillis() > expiry) {
            blacklist.remove(token);
            return false;
        }

        return true;
    }
}