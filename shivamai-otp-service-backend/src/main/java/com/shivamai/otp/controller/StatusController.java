package com.shivamai.otp.controller;

import com.shivamai.otp.dtoresponse.ApiResponse;
import com.shivamai.otp.enums.ServiceStatus;
import com.shivamai.otp.service.HealthService;
import com.shivamai.otp.dtoresponse.StatusResponse;

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
public class StatusController {

    private final HealthService healthService;

    @GetMapping
    public ResponseEntity<ApiResponse<StatusResponse>> getStatus() {

        log.debug("Public status requested");

        StatusResponse status = healthService.getPublicStatus();

        HttpStatus httpStatus = mapToHttpStatus(status.getStatus());

        return ResponseEntity.status(httpStatus)
                .body(new ApiResponse<>(
                        "SUCCESS",
                        "Service status",
                        status,
                        LocalDateTime.now()
                ));
    }

    private HttpStatus mapToHttpStatus(ServiceStatus status) {

        return switch (status) {
            case OPERATIONAL -> HttpStatus.OK;
            case DEGRADED -> HttpStatus.OK;
            case DOWN -> HttpStatus.SERVICE_UNAVAILABLE;
        };
    }
}