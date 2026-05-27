package com.shivamai.otp.webhook.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WebhookLogResponse {

    private Long id;

    private String targetUrl;

    private String status;

    private Integer responseStatusCode;

    private Integer retryCount;

    private String failureReason;

    private LocalDateTime deliveredAt;

    private LocalDateTime createdAt;
}