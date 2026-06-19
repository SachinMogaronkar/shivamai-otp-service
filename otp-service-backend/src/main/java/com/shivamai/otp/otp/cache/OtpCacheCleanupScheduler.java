package com.shivamai.otp.otp.cache;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OtpCacheCleanupScheduler {

    private final OtpSessionStore cacheManager;

    @Scheduled(fixedRate = 60000)
    public void cleanup() {

        cacheManager.removeExpiredOtps();
    }
}