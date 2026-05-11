package com.shivamai.otp.service;

import com.shivamai.otp.entity.ClientUsage;
import com.shivamai.otp.repository.ClientUsageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageService {

    private final ClientUsageRepository repository;

    public void recordOtpRequest(String clientId) {

        if (clientId == null || clientId.isBlank()) {
            return;
        }

        String key = ("usage:" + clientId).intern();

        synchronized (key) {

            ClientUsage usage = getOrCreate(clientId);

            usage.setOtpRequests(usage.getOtpRequests() + 1);

            repository.save(usage);

            log.debug("OTP request recorded for clientId: {}", clientId);
        }
    }

    public long getTodayUsage(String clientId) {

        if (clientId == null || clientId.isBlank()) {
            return 0;
        }

        return repository.findByClientIdAndDate(clientId, LocalDate.now())
                .map(ClientUsage::getOtpRequests)
                .orElse(0);
    }

    public void recordOtpVerification(String clientId) {

        if (clientId == null || clientId.isBlank()) {
            return;
        }

        String key = ("usage:" + clientId).intern();

        synchronized (key) {

            ClientUsage usage = getOrCreate(clientId);

            usage.setOtpVerified(usage.getOtpVerified() + 1);

            repository.save(usage);

            log.debug("OTP verification recorded for clientId: {}", clientId);
        }
    }

    // 🔹 Common method to avoid duplication
    private ClientUsage getOrCreate(String clientId) {

        LocalDate today = LocalDate.now();

        return repository.findByClientIdAndDate(clientId, today)
                .orElseGet(() -> ClientUsage.builder()
                        .clientId(clientId)
                        .date(today)
                        .otpRequests(0)
                        .otpVerified(0)
                        .createdAt(LocalDateTime.now())
                        .build());
    }
}