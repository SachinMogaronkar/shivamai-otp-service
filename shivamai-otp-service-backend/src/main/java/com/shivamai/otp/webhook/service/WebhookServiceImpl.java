package com.shivamai.otp.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.shivamai.otp.account.entity.DeveloperAccount;
import com.shivamai.otp.application.entity.DeveloperApplication;
import com.shivamai.otp.audit.dto.request.AuditLogRequest;
import com.shivamai.otp.audit.enums.AuditActorType;
import com.shivamai.otp.audit.enums.AuditEventType;
import com.shivamai.otp.audit.logging.ApiAccessLogger;

import com.shivamai.otp.otp.entity.OtpRequest;

import com.shivamai.otp.webhook.dto.WebhookSecretResponse;
import com.shivamai.otp.webhook.entity.WebhookLog;
import com.shivamai.otp.webhook.enums.WebhookEventType;
import com.shivamai.otp.webhook.enums.WebhookStatus;
import com.shivamai.otp.webhook.repository.WebhookLogRepository;
import com.shivamai.otp.webhook.util.WebhookSignatureUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookServiceImpl
        implements WebHookService {

    private static final int MAX_RETRY_COUNT =
            3;

    private final WebhookLogRepository repository;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    private final ApiAccessLogger apiAccessLogger;

    // =====================================
    // SEND EVENT
    // =====================================

    @Override
    public void sendEvent(
            String clientId,
            String url,
            String webhookSecret,
            WebhookEventType eventType,
            OtpRequest request
    ) {

        try {

            String payload =
                    buildPayload(
                            eventType,
                            request
                    );

            WebhookLog webhookLog =
                    WebhookLog.builder()
                            .targetUrl(url)
                            .clientId(clientId)
                            .payload(payload)
                            .eventType(eventType)
                            .webhookSecret(webhookSecret)
                            .status(WebhookStatus.PENDING)
                            .retryCount(0)
                            .retryInProgress(false)
                            .nextRetryAt(LocalDateTime.now())
                            .build();

            WebhookLog savedLog = repository.save(webhookLog);

            send(savedLog);

        } catch (Exception e) {

            log.error(
                    "Webhook processing failed for url={}",
                    url,
                    e
            );

            apiAccessLogger.logEvent(
                    AuditLogRequest.builder()
                            .clientId("SYSTEM")
                            .identifier(
                                    request.getIdentifier()
                            )
                            .actorType(
                                    AuditActorType.SYSTEM
                            )
                            .endpoint(
                                    "/webhook/send"
                            )
                            .eventType(
                                    AuditEventType.WEBHOOK_DELIVERY_FAILED
                            )
                            .ip("UNKNOWN")
                            .status(500)
                            .build()
            );
        }
    }

    // =====================================
    // SEND WEBHOOK
    // =====================================

    @Async
    public void send(
            WebhookLog webhookLog
    ) {

        try {

            HttpEntity<String> entity =
                    buildHttpEntity(
                            webhookLog.getPayload(),
                            webhookLog.getWebhookSecret(),
                            webhookLog.getEventType()
                    );

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            webhookLog.getTargetUrl(),
                            entity,
                            String.class
                    );

            webhookLog.setResponseStatusCode(
                    response.getStatusCode()
                            .value()
            );

            if (!response.getStatusCode()
                    .is2xxSuccessful()) {

                throw new RuntimeException(
                        "Webhook returned status="
                                + response.getStatusCode()
                );
            }

            webhookLog.setStatus(
                    WebhookStatus.SUCCESS
            );

            webhookLog.setDeliveredAt(
                    LocalDateTime.now()
            );

            webhookLog.setFailureReason(
                    null
            );

            webhookLog.setNextRetryAt(
                    null
            );

            log.info(
                    "Webhook delivered successfully to={}",
                    webhookLog.getTargetUrl()
            );

        } catch (Exception e) {

            int retryCount =
                    webhookLog.getRetryCount() + 1;

            webhookLog.setStatus(
                    WebhookStatus.FAILED
            );

            webhookLog.setRetryCount(
                    retryCount
            );

            webhookLog.setFailureReason(
                    e.getMessage()
            );

            webhookLog.setNextRetryAt(
                    calculateNextRetryTime(
                            retryCount
                    )
            );

            log.warn(
                    "Webhook delivery failed for={}, retryCount={}",
                    webhookLog.getTargetUrl(),
                    retryCount
            );

            apiAccessLogger.logEvent(
                    AuditLogRequest.builder()
                            .clientId("SYSTEM")
                            .identifier(
                                    webhookLog.getTargetUrl()
                            )
                            .actorType(
                                    AuditActorType.SYSTEM
                            )
                            .endpoint(
                                    "/webhook/send"
                            )
                            .eventType(
                                    AuditEventType.WEBHOOK_DELIVERY_FAILED
                            )
                            .ip("UNKNOWN")
                            .status(500)
                            .build()
            );
        }

        webhookLog.setRetryInProgress(
                false
        );

        repository.save(
                webhookLog
        );
    }

    // =====================================
    // RETRY FAILED WEBHOOKS
    // =====================================

    @Scheduled(fixedDelay = 60000)
    public void retryFailedWebhooks() {

        List<WebhookLog> failedLogs =
                repository
                        .findByStatusAndRetryInProgressFalseAndRetryCountLessThanAndNextRetryAtBefore(
                                WebhookStatus.FAILED,
                                MAX_RETRY_COUNT,
                                LocalDateTime.now()
                        );

        for (WebhookLog webhookLog : failedLogs) {

            try {

                webhookLog.setRetryInProgress(
                        true
                );

                repository.save(
                        webhookLog
                );

                send(
                        webhookLog
                );

            } catch (Exception e) {

                webhookLog.setRetryInProgress(
                        false
                );

                repository.save(
                        webhookLog
                );

                log.error(
                        "Webhook retry failed for={}",
                        webhookLog.getTargetUrl(),
                        e
                );
            }
        }
    }

    // =====================================
    // RETRY STRATEGY
    // =====================================

    private LocalDateTime calculateNextRetryTime(
            int retryCount
    ) {

        return switch (retryCount) {

            case 1 ->
                    LocalDateTime.now()
                            .plusMinutes(1);

            case 2 ->
                    LocalDateTime.now()
                            .plusMinutes(5);

            default ->
                    LocalDateTime.now()
                            .plusMinutes(15);
        };
    }

    // =====================================
    // PAYLOAD BUILDER
    // =====================================

    private String buildPayload(
            WebhookEventType eventType,
            OtpRequest request
    ) throws Exception {

        LocalDateTime now =
                LocalDateTime.now();

        Map<String, Object> payload =
                Map.of(
                        "eventId",
                        request.getId(),

                        "eventType",
                        eventType,

                        "eventTimestamp",
                        now,

                        "deliveredAt",
                        now,

                        "identifier",
                        request.getIdentifier(),

                        "requestId",
                        request.getId(),

                        "status",
                        request.getStatus(),

                        "verifiedAt",
                        request.getVerifiedAt(),

                        "otpType",
                        request.getOtpType(),

                        "channel",
                        request.getChannel()
                );

        return objectMapper.writeValueAsString(
                payload
        );
    }

    // =====================================
    // HTTP ENTITY
    // =====================================

    private HttpEntity<String> buildHttpEntity(
            String payload,
            String webhookSecret,
            WebhookEventType eventType
    ) {

        HttpHeaders headers =
                new HttpHeaders();

        String timestamp =
                String.valueOf(
                        System.currentTimeMillis()
                );

        String signature =
                WebhookSignatureUtil.generateSignature(
                        webhookSecret,
                        payload,
                        timestamp
                );

        headers.set(
                "X-Shivamai-Signature",
                signature
        );

        headers.set(
                "X-Shivamai-Timestamp",
                timestamp
        );

        headers.set(
                "X-Shivamai-Event",
                eventType.name()
        );

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        return new HttpEntity<>(
                payload,
                headers
        );
    }
}