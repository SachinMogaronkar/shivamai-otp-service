package com.shivamai.otp.otp.dto.request;

import com.shivamai.otp.otp.enums.OtpPurpose;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OtpRequestDTO {

    @NotBlank(message = "Identifier is required")
    @Size(max = 150)
    private String identifier;

    @NotBlank(message = "Display name is required")
    @Size(max = 120)
    private String displayName;

    @NotBlank(message = "Application name is required")
    @Size(max = 120)
    private String applicationName;

    @NotNull(message = "Purpose is required")
    private OtpPurpose purpose;
}