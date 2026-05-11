package com.shivamai.otp.controller;

import com.shivamai.otp.dtoresponse.ApiResponse;
import com.shivamai.otp.dtoresponse.UsageResponse;
import com.shivamai.otp.entity.ClientUsage;
import com.shivamai.otp.exception.InvalidRequestException;
import com.shivamai.otp.exception.ResourceNotFoundException;
import com.shivamai.otp.repository.ClientUsageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/usage")
@Slf4j
public class AdminUsageController {

    private final ClientUsageRepository repository;

    @GetMapping
    public ApiResponse<List<UsageResponse>> getUsage() {

        log.info("Fetching all usage data");

        List<UsageResponse> usage =
                repository.findAll()
                        .stream()
                        .map(u -> UsageResponse.builder()
                                .clientId(u.getClientId())
                                .date(u.getDate())
                                .otpRequests(u.getOtpRequests())
                                .otpVerified(u.getOtpVerified())
                                .build())
                        .toList();

        return new ApiResponse<>(
                "SUCCESS",
                "Platform usage",
                usage,
                LocalDateTime.now()
        );
    }

    @GetMapping("/{clientId}")
    public ApiResponse<List<UsageResponse>> getUsageForClient(@PathVariable String clientId) {

        if (clientId == null || clientId.isBlank()) {
            throw new InvalidRequestException("ClientId is required");
        }

        log.info("Fetching usage for clientId={}", clientId);

        List<UsageResponse> usage =
                repository.findByClientId(clientId)
                        .stream()
                        .map(u -> UsageResponse.builder()
                                .clientId(u.getClientId())
                                .date(u.getDate())
                                .otpRequests(u.getOtpRequests())
                                .otpVerified(u.getOtpVerified())
                                .build())
                        .toList();

        return new ApiResponse<>(
                "SUCCESS",
                "Usage for client",
                usage,
                LocalDateTime.now()
        );
    }
}