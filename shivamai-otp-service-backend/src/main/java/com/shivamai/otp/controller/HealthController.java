package com.shivamai.otp.controller;

import com.shivamai.otp.dtoresponse.ApiResponse;
import com.shivamai.otp.repository.OtpRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/health/internal")
@Slf4j
public class HealthController {

    private final OtpRequestRepository repository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {

        Map<String, String> status = new HashMap<>();

        try {

            repository.count();

            status.put("database", "UP");

            log.debug("Health check OK");

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            "SUCCESS",
                            "Service is healthy",
                            status,
                            LocalDateTime.now()
                    )
            );

        } catch (Exception e) {

            log.error("Health check failed", e);

            status.put("database", "DOWN");

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ApiResponse<>(
                            "FAILED",
                            "Service is unhealthy",
                            status,
                            LocalDateTime.now()
                    ));
        }
    }
}