package com.shivamai.otp.webhook.service;

import com.shivamai.otp.otp.entity.OtpRequest;
import com.shivamai.otp.webhook.dto.ApplicationWebhookLogResponse;
import com.shivamai.otp.webhook.dto.WebhookSecretResponse;
import com.shivamai.otp.webhook.enums.WebhookEventType;

import java.util.List;

public interface WebHookService {
    void sendEvent(
            String clientId,
            String url,
            String webhookSecret,
            WebhookEventType eventType,
            OtpRequest request
    );
}