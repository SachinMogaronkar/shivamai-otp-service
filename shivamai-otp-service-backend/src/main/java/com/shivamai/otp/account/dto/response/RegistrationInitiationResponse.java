package com.shivamai.otp.account.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistrationInitiationResponse {

    private String identifier;

    private Long requestId;

    private int expirySeconds;

    private String channel;

    private String status;
}