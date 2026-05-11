package com.shivamai.otp.security;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class OtpRateLimiter {

    private static final int MAX_REQUESTS = 30;
    private static final long WINDOW = 60000;

    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> requests
            = new ConcurrentHashMap<>();

    public boolean isAllowed(String key) {

        long now = System.currentTimeMillis();

        requests.putIfAbsent(key, new ConcurrentLinkedQueue<>());

        ConcurrentLinkedQueue<Long> queue = requests.get(key);

        while (!queue.isEmpty() && now - queue.peek() > WINDOW) {
            queue.poll();
        }

        if (queue.size() >= MAX_REQUESTS) {
            return false;
        }

        queue.add(now);

        return true;
    }
}