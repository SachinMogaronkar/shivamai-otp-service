package com.shivamai.otp.health.controller;

import com.shivamai.otp.common.response.ApiResponse;

import com.shivamai.otp.otp.repository.OtpRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/health/internal")
@Slf4j
public class HealthController {

    private final OtpRequestRepository repository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>>
    health() {

        try {

            repository.count();

            log.debug(
                    "Health check successful"
            );

            return response(
                    HttpStatus.OK,
                    "SUCCESS",
                    "Service is healthy",
                    Map.of(
                            "database",
                            "UP"
                    )
            );

        } catch (Exception e) {

            log.error(
                    "Health check failed",
                    e
            );

            return response(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "FAILED",
                    "Service is unhealthy",
                    Map.of(
                            "database",
                            "DOWN"
                    )
            );
        }
    }

    // =====================================
    // COMMON RESPONSE BUILDER
    // =====================================

    private ResponseEntity<ApiResponse<Map<String, String>>>
    response(
            HttpStatus status,
            String result,
            String message,
            Map<String, String> data
    ) {

        return ResponseEntity.status(status)
                .body(
                        new ApiResponse<>(
                                result,
                                message,
                                data,
                                LocalDateTime.now()
                        )
                );
    }
}