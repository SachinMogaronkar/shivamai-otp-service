package com.shivamai.otp.otp.service;

import com.shivamai.otp.application.entity.DeveloperApplication;
import com.shivamai.otp.application.repository.DeveloperApplicationRepository;

import com.shivamai.otp.audit.enums.AuditEventType;
import com.shivamai.otp.audit.service.AuditService;

import com.shivamai.otp.common.exception.OtpVerificationException;

import com.shivamai.otp.otp.cache.ActiveOtpSession;
import com.shivamai.otp.otp.cache.OtpSessionStore;

import com.shivamai.otp.otp.dto.response.OtpVerificationResponse;

import com.shivamai.otp.otp.entity.OtpRequest;

import com.shivamai.otp.otp.enums.OtpStatus;

import com.shivamai.otp.otp.repository.OtpRequestRepository;

import com.shivamai.otp.usage.service.OtpUsageService;

import com.shivamai.otp.webhook.enums.WebhookEventType;
import com.shivamai.otp.webhook.service.WebHookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpVerificationServiceImpl
        implements OtpVerificationService {

    private static final int MAX_ATTEMPTS =
            3;

    private static final String VERIFY_ENDPOINT =
            "/otp/verify";

    private static final int HTTP_OK =
            200;

    private static final int HTTP_ERROR =
            500;

    private final OtpRequestRepository repository;

    private final OtpSessionStore cacheManager;

    private final OtpUsageService otpUsageService;

    private final WebHookService webhookService;

    private final DeveloperApplicationRepository developerApplicationRepository;

    private final AuditService auditService;

    private final RequestMetadataService requestMetadataService;

    @Override
    @Transactional
    public OtpVerificationResponse verifyOtp(
            String identifier,
            Long requestId,
            String otp
    ) {

        validateInput(
                requestId,
                otp
        );

        RequestMetadata metadata =
                requestMetadataService.extract();

        OtpRequest otpRequest =
                repository.findById(
                                requestId
                        )
                        .orElseThrow(
                                () -> new OtpVerificationException(
                                        "OTP request not found"
                                )
                        );

        if (!Objects.equals(
                otpRequest.getIdentifier(),
                identifier
        )) {

            auditService.logSystemEvent(
                    identifier,
                    VERIFY_ENDPOINT,
                    AuditEventType.OTP_VERIFICATION_FAILED,
                    HTTP_ERROR
            );

            throw new OtpVerificationException(
                    "Identifier mismatch"
            );
        }

        if (otpRequest.getStatus()
                != OtpStatus.DELIVERED) {

            auditService.logSystemEvent(
                    identifier,
                    VERIFY_ENDPOINT,
                    AuditEventType.OTP_VERIFICATION_FAILED,
                    HTTP_ERROR
            );

            throw new OtpVerificationException(
                    "OTP already used or invalid"
            );
        }

        ActiveOtpSession cached =
                cacheManager.getOtp(
                        identifier,
                        requestId
                );

        if (cached == null) {

            expireOtp(
                    otpRequest,
                    identifier,
                    requestId,
                    metadata.clientId()
            );

            auditService.logSystemEvent(
                    identifier,
                    VERIFY_ENDPOINT,
                    AuditEventType.OTP_EXPIRED,
                    HTTP_ERROR
            );

            throw new OtpVerificationException(
                    "OTP expired"
            );
        }

        if (!Objects.equals(
                cached.getApplicationName(),
                otpRequest.getApplicationName()
        )) {

            auditService.logSystemEvent(
                    identifier,
                    VERIFY_ENDPOINT,
                    AuditEventType.OTP_VERIFICATION_FAILED,
                    HTTP_ERROR
            );

            throw new OtpVerificationException(
                    "OTP application mismatch"
            );
        }

        if (cached.getPurpose()
                != otpRequest.getPurpose()) {

            auditService.logSystemEvent(
                    identifier,
                    VERIFY_ENDPOINT,
                    AuditEventType.OTP_VERIFICATION_FAILED,
                    HTTP_ERROR
            );

            throw new OtpVerificationException(
                    "OTP purpose mismatch"
            );
        }

        if (System.currentTimeMillis()
                >= cached.getExpiryTime()) {

            expireOtp(
                    otpRequest,
                    identifier,
                    requestId,
                    metadata.clientId()
            );

            auditService.logSystemEvent(
                    identifier,
                    VERIFY_ENDPOINT,
                    AuditEventType.OTP_EXPIRED,
                    HTTP_ERROR
            );

            throw new OtpVerificationException(
                    "OTP expired"
            );
        }

        int attempts =
                otpRequest.getAttemptCount() + 1;

        otpRequest.setAttemptCount(
                attempts
        );

        String hash =
                OtpHashUtil.hash(
                        otp
                );

        if (!hash.equals(
                cached.getOtpHash()
        )) {

            if (attempts >= MAX_ATTEMPTS) {

                otpRequest.setStatus(
                        OtpStatus.BLOCKED
                );

                repository.save(
                        otpRequest
                );

                cacheManager.removeOtp(
                        identifier,
                        requestId
                );

                auditService.logSystemEvent(
                        identifier,
                        VERIFY_ENDPOINT,
                        AuditEventType.OTP_BLOCKED,
                        HTTP_ERROR
                );

                if (metadata.clientId() != null) {

                    sendWebhookIfConfigured(
                            metadata.clientId(),
                            otpRequest,
                            WebhookEventType.OTP_FAILED
                    );
                }

                throw new OtpVerificationException(
                        "Maximum attempts exceeded"
                );
            }

            repository.save(
                    otpRequest
            );

            auditService.logSystemEvent(
                    identifier,
                    VERIFY_ENDPOINT,
                    AuditEventType.OTP_INVALID,
                    HTTP_ERROR
            );

            throw new OtpVerificationException(
                    "Invalid OTP"
            );
        }

        cacheManager.removeOtp(
                identifier,
                requestId
        );

        otpRequest.setStatus(
                OtpStatus.VERIFIED
        );

        LocalDateTime verifiedAt =
                LocalDateTime.now();

        otpRequest.setVerifiedAt(
                verifiedAt
        );

        repository.save(
                otpRequest
        );

        auditService.logSystemEvent(
                identifier,
                VERIFY_ENDPOINT,
                AuditEventType.OTP_VERIFIED,
                HTTP_OK
        );

        if (metadata.clientId() != null) {

            otpUsageService.recordOtpVerification(
                    metadata.clientId()
            );

            sendWebhookIfConfigured(
                    metadata.clientId(),
                    otpRequest,
                    WebhookEventType.OTP_VERIFIED
            );
        }

        log.info(
                "OTP verified successfully identifier={}",
                identifier
        );

        return OtpVerificationResponse.builder()
                .verified(true)
                .requestId(otpRequest.getId())
                .identifier(otpRequest.getIdentifier())
                .applicationName(
                        otpRequest.getApplicationName()
                )
                .purpose(
                        otpRequest.getPurpose()
                )
                .verifiedAt(
                        verifiedAt
                )
                .build();
    }

    @Override
    public void ping() {

        repository.count();
    }

    private void validateInput(
            Long requestId,
            String otp
    ) {

        if (requestId == null) {

            throw new OtpVerificationException(
                    "RequestId is required"
            );
        }

        if (otp == null
                || otp.isBlank()) {

            throw new OtpVerificationException(
                    "OTP is required"
            );
        }
    }

    private void expireOtp(
            OtpRequest otpRequest,
            String identifier,
            Long requestId,
            String clientId
    ) {

        otpRequest.setStatus(
                OtpStatus.EXPIRED
        );

        repository.save(
                otpRequest
        );

        cacheManager.removeOtp(
                identifier,
                requestId
        );

        if (clientId != null) {

            sendWebhookIfConfigured(
                    clientId,
                    otpRequest,
                    WebhookEventType.OTP_EXPIRED
            );
        }
    }

    private void sendWebhookIfConfigured(
            String clientId,
            OtpRequest otpRequest,
            WebhookEventType eventType
    ) {

        DeveloperApplication app =
                developerApplicationRepository
                        .findByClientId(
                                clientId
                        )
                        .orElse(null);

        if (app == null
                || app.getWebhookUrl() == null
                || app.getWebhookUrl().isBlank()) {

            return;
        }

        try {

            webhookService.sendEvent(
                    app.getClientId(),
                    app.getWebhookUrl(),
                    app.getWebhookSecret(),
                    WebhookEventType.OTP_VERIFIED,
                    otpRequest
            );

        } catch (Exception e) {

            log.error(
                    "Webhook dispatch failed clientId={}",
                    clientId,
                    e
            );

            auditService.logSystemEvent(
                    otpRequest.getIdentifier(),
                    VERIFY_ENDPOINT,
                    AuditEventType.WEBHOOK_DELIVERY_FAILED,
                    HTTP_ERROR
            );
        }
    }
}