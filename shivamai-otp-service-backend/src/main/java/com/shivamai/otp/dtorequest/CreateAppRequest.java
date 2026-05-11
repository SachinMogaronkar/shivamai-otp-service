package com.shivamai.otp.dtorequest;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAppRequest {

    @NotBlank(message = "App name is required")
    private String appName;

    @NotBlank(message = "Webhook URL is required")   // ✅ ADD
    private String webhookUrl;
}