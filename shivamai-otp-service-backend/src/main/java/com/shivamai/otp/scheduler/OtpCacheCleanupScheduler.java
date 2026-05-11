package com.shivamai.otp.scheduler;

import com.shivamai.otp.cache.OtpCacheManager;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OtpCacheCleanupScheduler {

    private final OtpCacheManager cacheManager;

    public OtpCacheCleanupScheduler(OtpCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedRate = 60000)
    public void cleanup() {
        cacheManager.removeExpiredOtps();
    }
}