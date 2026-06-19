package com.shivamai.otp.account.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RegistrationInitiationResponse {

    private String identifier;

    private Long requestId;

    private LocalDateTime expiresAt;

    private long remainingSeconds;

    private String channel;

    private String status;
}