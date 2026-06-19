package com.shivamai.otp.account.dto.response;

import com.shivamai.otp.account.enums.DeveloperAccountStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistrationVerificationResponse {

    private String identifier;

    private DeveloperAccountStatus status;
}