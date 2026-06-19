package com.shivamai.otp.common.security;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
public class OtpRateLimiter {

    @Value("${otp.rate-limit.max-requests}")
    private int maxRequests;

    @Value("${otp.rate-limit.window-ms}")
    private long windowMs;

    private final ConcurrentHashMap<
            String,
            ConcurrentLinkedQueue<Long>
            > requests =
            new ConcurrentHashMap<>();

    public boolean isAllowed(
            String key
    ) {

        long now =
                System.currentTimeMillis();

        ConcurrentLinkedQueue<Long> queue =
                requests.computeIfAbsent(
                        key,
                        k -> new ConcurrentLinkedQueue<>()
                );

        // =====================================
        // REMOVE EXPIRED REQUESTS
        // =====================================

        while (!queue.isEmpty()
                && now - queue.peek() > windowMs) {

            queue.poll();
        }

        // =====================================
        // CLEAN EMPTY QUEUES
        // =====================================

        if (queue.isEmpty()) {

            requests.remove(
                    key,
                    queue
            );
        }

        // =====================================
        // RATE LIMIT CHECK
        // =====================================

        if (queue.size() >= maxRequests) {

            log.warn(
                    "OTP rate limit exceeded for key={}",
                    key
            );

            return false;
        }

        // =====================================
        // RECORD REQUEST
        // =====================================

        queue.add(now);

        return true;
    }
}