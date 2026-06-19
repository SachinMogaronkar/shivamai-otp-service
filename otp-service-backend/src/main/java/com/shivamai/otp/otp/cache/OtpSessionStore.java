package com.shivamai.otp.otp.cache;

import com.shivamai.otp.common.exception.OtpException;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpSessionStore {

    private final ConcurrentHashMap<String, ActiveOtpSession> cache =
            new ConcurrentHashMap<>();

    private String buildKey(
            String identifier,
            Long requestId
    ) {

        return identifier
                + ":"
                + requestId;
    }

    public void storeOtp(
            ActiveOtpSession session
    ) {

        if (session == null) {

            throw new OtpException(
                    "OTP session cannot be null"
            );
        }

        if (session.getIdentifier() == null
                || session.getRequestId() == null) {

            throw new OtpException(
                    "Invalid OTP session"
            );
        }

        cache.put(
                buildKey(
                        session.getIdentifier(),
                        session.getRequestId()
                ),
                session
        );
    }

    public ActiveOtpSession getOtp(
            String identifier,
            Long requestId
    ) {

        String key =
                buildKey(
                        identifier,
                        requestId
                );

        ActiveOtpSession cached =
                cache.get(
                        key
                );

        if (cached == null) {

            return null;
        }

        if (System.currentTimeMillis()
                >= cached.getExpiryTime()) {

            cache.remove(
                    key
            );

            return null;
        }

        return cached;
    }

    public void removeOtp(
            String identifier,
            Long requestId
    ) {

        cache.remove(
                buildKey(
                        identifier,
                        requestId
                )
        );
    }

    public void removeExpiredOtps() {

        long now =
                System.currentTimeMillis();

        cache.entrySet().removeIf(
                entry ->
                        entry.getValue()
                                .getExpiryTime() <= now
        );
    }

    public int activeSessions() {

        removeExpiredOtps();

        return cache.size();
    }

    public void ping() {

        if (cache == null) {

            throw new OtpException(
                    "OTP cache unavailable"
            );
        }
    }
}