package com.shivamai.otp.cache;

import com.shivamai.otp.exception.OtpException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpCacheManager {

    private final ConcurrentHashMap<String, CachedOtp> cache =
            new ConcurrentHashMap<>();

    private String buildKey(String identifier, Long requestId) {
        return identifier + ":" + requestId;
    }

    public void storeOtp(String identifier,
                         Long requestId,
                         String otp,
                         String hash,
                         long expiry) {

        cache.put(buildKey(identifier, requestId),
                new CachedOtp(requestId, otp, hash, expiry));
    }

    public CachedOtp getOtp(String identifier, Long requestId) {

        String key = buildKey(identifier, requestId);

        CachedOtp cached = cache.get(key);

        if (cached == null) {
            return null;
        }

        if (System.currentTimeMillis() >= cached.getExpiryTime()) {

            cache.remove(key);

            return null;
        }

        return cached;
    }

    public void removeOtp(String identifier, Long requestId) {
        cache.remove(buildKey(identifier, requestId));
    }

    public void removeExpiredOtps() {

        long now = System.currentTimeMillis();

        cache.entrySet().removeIf(
                entry -> entry.getValue().getExpiryTime() < now
        );
    }

    public void ping() {

        // simplest check
        if (this == null) {
            throw new OtpException("Cache not initialized");
        }
    }
}