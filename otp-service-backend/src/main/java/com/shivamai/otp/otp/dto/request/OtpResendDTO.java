package com.shivamai.otp.otp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OtpResendDTO {

    @NotBlank(message = "Identifier is required")
    private String identifier;

    @NotNull(message = "RequestId is required")
    private Long requestId;
}