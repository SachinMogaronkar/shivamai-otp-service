package com.shivamai.otp.application.dto.response;

import com.shivamai.otp.application.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class ApplicationDetailResponse {
    private Long id;
    private String applicationName;
    private String clientId;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
}
