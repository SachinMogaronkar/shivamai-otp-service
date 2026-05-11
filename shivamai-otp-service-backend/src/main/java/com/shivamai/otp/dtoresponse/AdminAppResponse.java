package com.shivamai.otp.dtoresponse;

import com.shivamai.otp.enums.AppStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class AdminAppResponse {
    private Long id;
    private String appName;
    private String clientId;
    @Enumerated(EnumType.STRING)
    private AppStatus status;
    private LocalDateTime createdAt;
}
