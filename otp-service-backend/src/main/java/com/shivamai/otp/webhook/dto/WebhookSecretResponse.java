package com.shivamai.otp.webhook.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebhookSecretResponse {

    private String webhookSecret;

    private String message;
}