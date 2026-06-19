package com.shivamai.otp.webhook.dto;

import com.shivamai.otp.webhook.enums.WebhookEventType;
import com.shivamai.otp.webhook.enums.WebhookStatus;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApplicationWebhookLogResponse {

    private Long id;

    private WebhookEventType eventType;

    private WebhookStatus status;

    private Integer responseStatusCode;

    private Integer retryCount;

    private String failureReason;

    private LocalDateTime deliveredAt;

    private LocalDateTime createdAt;
}