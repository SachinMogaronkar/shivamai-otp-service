package com.shivamai.otp.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateApplicationRequest {

    @NotBlank(message = "App name is required")
    private String applicationName;

    @NotBlank(message = "Webhook URL is required")   // ✅ ADD
    @Size(max = 500)
    @Pattern(
            regexp = "^(https://).+",
            message = "Webhook URL must start with https://"
    )
    private String webhookUrl;
}