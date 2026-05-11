package com.shivamai.otp.dtoresponse;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeveloperAppView {

    private Long id;
    private String appName;
    private String clientId;
    private String status;
    private LocalDateTime createdAt;
}