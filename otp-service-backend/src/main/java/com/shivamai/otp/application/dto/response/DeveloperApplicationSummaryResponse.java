package com.shivamai.otp.application.dto.response;

import com.shivamai.otp.application.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeveloperApplicationSummaryResponse {

    private Long id;

    private String applicationName;

    private String clientId;

    private LocalDateTime secretRotatedAt;

    private ApplicationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String message;
}