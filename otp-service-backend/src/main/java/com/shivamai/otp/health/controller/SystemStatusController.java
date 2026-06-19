package com.shivamai.otp.health.controller;

import com.shivamai.otp.common.response.ApiResponse;

import com.shivamai.otp.health.dto.SystemStatusResponse;
import com.shivamai.otp.health.enums.ServiceStatus;

import com.shivamai.otp.health.service.HealthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/status")
@Slf4j
public class SystemStatusController {

    private final HealthService healthService;

    @GetMapping
    public ResponseEntity<ApiResponse<SystemStatusResponse>>
    getStatus() {

        log.debug(
                "Public status requested"
        );

        SystemStatusResponse status =
                healthService.getPublicStatus();

        return response(
                mapToHttpStatus(
                        status.getStatus()
                ),
                "Service status",
                status
        );
    }

    // =====================================
    // STATUS MAPPING
    // =====================================

    private HttpStatus mapToHttpStatus(
            ServiceStatus status
    ) {

        if (status == null) {

            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        return switch (status) {

            case OPERATIONAL,
                 DEGRADED -> HttpStatus.OK;

            case DOWN ->
                    HttpStatus.SERVICE_UNAVAILABLE;
        };
    }

    // =====================================
    // COMMON RESPONSE
    // =====================================

    private ResponseEntity<ApiResponse<SystemStatusResponse>>
    response(
            HttpStatus status,
            String message,
            SystemStatusResponse data
    ) {

        return ResponseEntity.status(status)
                .body(
                        new ApiResponse<>(
                                "SUCCESS",
                                message,
                                data,
                                LocalDateTime.now()
                        )
                );
    }
}