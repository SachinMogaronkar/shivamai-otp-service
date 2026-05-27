package com.shivamai.otp.otp.service;

import com.shivamai.otp.application.entity.DeveloperApplication;
import com.shivamai.otp.application.enums.ApplicationStatus;
import com.shivamai.otp.application.repository.DeveloperApplicationRepository;

import com.shivamai.otp.audit.enums.AuditEventType;
import com.shivamai.otp.audit.service.AuditService;

import com.shivamai.otp.common.exception.ForbiddenOperationException;
import com.shivamai.otp.common.exception.OtpException;
import com.shivamai.otp.common.exception.RateLimitExceededException;

import com.shivamai.otp.otp.cache.ActiveOtpSession;
import com.shivamai.otp.otp.cache.OtpSessionStore;

import com.shivamai.otp.otp.channel.ChannelDeliveryRouter;
import com.shivamai.otp.otp.channel.OtpDeliveryResult;

import com.shivamai.otp.otp.dto.OtpDeliveryContext;
import com.shivamai.otp.otp.dto.request.OtpRequestDTO;
import com.shivamai.otp.otp.dto.response.OtpDeliveryResponse;

import com.shivamai.otp.otp.entity.OtpRequest;

import com.shivamai.otp.otp.enums.OtpStatus;
import com.shivamai.otp.otp.enums.OtpType;

import com.shivamai.otp.otp.repository.OtpRequestRepository;

import com.shivamai.otp.usage.service.OtpUsageService;

import com.shivamai.otp.webhook.enums.WebhookEventType;
import com.shivamai.otp.webhook.service.WebHookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OtpServiceImpl
        implements OtpService {

    private static final String OTP_REQUEST_ENDPOINT =
            "/otp/request";

    private static final String OTP_RESEND_ENDPOINT =
            "/otp/resend";

    private static final int PER_MINUTE_LIMIT =
            20;

    private static final int DAILY_LIMIT =
            100;

    private static final int APPLICATION_PER_MINUTE_LIMIT =
            200;

    private static final int HTTP_OK =
            200;

    private static final int HTTP_ERROR =
            500;

    private final OtpRequestRepository repository;

    private final OtpSessionStore cacheManager;

    private final ChannelDeliveryRouter channelDeliveryRouter;

    private final OtpUsageService otpUsageService;

    private final AuditService auditService;

    private final RequestMetadataService requestMetadataService;

    private final DeveloperApplicationRepository appRepository;

    private final WebHookService webhookService;

    @Value("${otp.expiry.seconds}")
    private int expirySeconds;

    @Override
    public OtpDeliveryResponse requestOtp(
            OtpRequestDTO request
    ) {

        String identifier =
                request.getIdentifier();

        String applicationName =
                request.getApplicationName();

        RequestMetadata metadata =
                requestMetadataService.extract();

        validateApplicationAccess(
                metadata.clientId()
        );

        validateDailyLimit(
                identifier,
                metadata.clientId()
        );

        validatePerMinuteLimit(
                identifier
        );

        validateApplicationPerMinuteLimit(
                applicationName
        );

        auditService.logSystemEvent(
                identifier,
                OTP_REQUEST_ENDPOINT,
                AuditEventType.OTP_REQUESTED,
                HTTP_OK
        );

        Optional<OtpRequest> existing =
                repository
                        .findTopByIdentifierAndApplicationNameAndPurposeAndStatusInAndExpiresAtAfterOrderByCreatedAtDesc(
                                identifier,
                                applicationName,
                                request.getPurpose(),
                                List.of(OtpStatus.DELIVERED),
                                LocalDateTime.now()
                        );

        if (existing.isPresent()) {

            OtpRequest existingRequest =
                    existing.get();

            ActiveOtpSession cached =
                    cacheManager.getOtp(
                            identifier,
                            existingRequest.getId()
                    );

            if (cached != null) {

                OtpDeliveryResult reusedDelivery =
                        deliverOtp(
                                OtpDeliveryContext.builder()
                                        .identifier(
                                                existingRequest.getIdentifier()
                                        )
                                        .otp(
                                                cached.getOtp()
                                        )
                                        .displayName(
                                                existingRequest.getDisplayName()
                                        )
                                        .applicationName(
                                                existingRequest.getApplicationName()
                                        )
                                        .purpose(
                                                existingRequest.getPurpose()
                                        )
                                        .otpType(
                                                existingRequest.getOtpType()
                                        )
                                        .expirySeconds(
                                                expirySeconds
                                        )
                                        .build()
                        );

                if (!reusedDelivery.isSuccess()) {

                    auditService.logSystemEvent(
                            identifier,
                            OTP_REQUEST_ENDPOINT,
                            AuditEventType.OTP_REQUEST_FAILED,
                            HTTP_ERROR
                    );

                    throw new OtpException(
                            "OTP delivery failed"
                    );
                }

                auditService.logSystemEvent(
                        identifier,
                        OTP_REQUEST_ENDPOINT,
                        AuditEventType.OTP_RESENT,
                        HTTP_OK
                );

                return OtpDeliveryResponse.builder()
                        .channel(
                                reusedDelivery.getChannelUsed()
                        )
                        .expirySeconds(
                                expirySeconds
                        )
                        .requestId(
                                existingRequest.getId()
                        )
                        .build();
            }

            existingRequest.setStatus(
                    OtpStatus.EXPIRED
            );

            repository.save(
                    existingRequest
            );
        }

        String otp =
                OtpGenerator.generateOtp();

        String hash =
                OtpHashUtil.hash(
                        otp
                );

        OtpRequest otpRequest =
                createOtpRequest(
                        request,
                        hash
                );

        OtpDeliveryResult result =
                deliverOtp(
                        OtpDeliveryContext.builder()
                                .identifier(
                                        otpRequest.getIdentifier()
                                )
                                .otp(
                                        otp
                                )
                                .displayName(
                                        otpRequest.getDisplayName()
                                )
                                .applicationName(
                                        otpRequest.getApplicationName()
                                )
                                .purpose(
                                        otpRequest.getPurpose()
                                )
                                .otpType(
                                        otpRequest.getOtpType()
                                )
                                .expirySeconds(
                                        expirySeconds
                                )
                                .build()
                );

        if (!result.isSuccess()) {

            otpRequest.setStatus(
                    OtpStatus.DELIVERY_FAILED
            );

            repository.save(
                    otpRequest
            );

            auditService.logSystemEvent(
                    identifier,
                    OTP_REQUEST_ENDPOINT,
                    AuditEventType.OTP_REQUEST_FAILED,
                    HTTP_ERROR
            );

            throw new OtpException(
                    "OTP delivery failed"
            );
        }

        cacheManager.storeOtp(
                ActiveOtpSession.builder()
                        .requestId(
                                otpRequest.getId()
                        )
                        .identifier(
                                otpRequest.getIdentifier()
                        )
                        .otp(
                                otp
                        )
                        .otpHash(
                                hash
                        )
                        .displayName(
                                otpRequest.getDisplayName()
                        )
                        .applicationName(
                                otpRequest.getApplicationName()
                        )
                        .purpose(
                                otpRequest.getPurpose()
                        )
                        .expiryTime(
                                System.currentTimeMillis()
                                        + expirySeconds * 1000L
                        )
                        .build()
        );

        otpRequest.setChannel(
                result.getChannelUsed()
        );

        otpRequest.setStatus(
                OtpStatus.DELIVERED
        );

        repository.save(
                otpRequest
        );

        if (metadata.clientId() != null) {

            otpUsageService.recordOtpRequest(
                    metadata.clientId()
            );

            sendWebhookIfConfigured(
                    metadata.clientId(),
                    otpRequest,
                    WebhookEventType.OTP_REQUESTED
            );
        }

        auditService.logSystemEvent(
                identifier,
                OTP_REQUEST_ENDPOINT,
                AuditEventType.OTP_DELIVERED,
                HTTP_OK
        );

        return OtpDeliveryResponse.builder()
                .channel(
                        result.getChannelUsed()
                )
                .expirySeconds(
                        expirySeconds
                )
                .requestId(
                        otpRequest.getId()
                )
                .build();
    }

    @Override
    public OtpDeliveryResponse resendOtp(
            String identifier,
            Long requestId
    ) {

        OtpRequest otpRequest =
                repository.findById(
                                requestId
                        )
                        .orElseThrow(
                                () -> new OtpException(
                                        "OTP request not found"
                                )
                        );

        if (!otpRequest.getIdentifier().equals(
                identifier
        )) {

            throw new OtpException(
                    "Identifier mismatch"
            );
        }

        if (otpRequest.getStatus()
                != OtpStatus.DELIVERED) {

            throw new OtpException(
                    "OTP cannot be resent"
            );
        }

        ActiveOtpSession cached =
                cacheManager.getOtp(
                        identifier,
                        requestId
                );

        if (cached == null) {

            otpRequest.setStatus(
                    OtpStatus.EXPIRED
            );

            repository.save(
                    otpRequest
            );

            throw new OtpException(
                    "OTP expired"
            );
        }

        OtpDeliveryResult result =
                deliverOtp(
                        OtpDeliveryContext.builder()
                                .identifier(
                                        otpRequest.getIdentifier()
                                )
                                .otp(
                                        cached.getOtp()
                                )
                                .displayName(
                                        otpRequest.getDisplayName()
                                )
                                .applicationName(
                                        otpRequest.getApplicationName()
                                )
                                .purpose(
                                        otpRequest.getPurpose()
                                )
                                .otpType(
                                        otpRequest.getOtpType()
                                )
                                .expirySeconds(
                                        expirySeconds
                                )
                                .build()
                );

        if (!result.isSuccess()) {

            auditService.logSystemEvent(
                    identifier,
                    OTP_RESEND_ENDPOINT,
                    AuditEventType.OTP_RESEND_FAILED,
                    HTTP_ERROR
            );

            throw new OtpException(
                    "OTP resend failed"
            );
        }

        otpRequest.setLastResendAt(
                LocalDateTime.now()
        );

        repository.save(
                otpRequest
        );

        auditService.logSystemEvent(
                identifier,
                OTP_RESEND_ENDPOINT,
                AuditEventType.OTP_RESENT,
                HTTP_OK
        );

        return OtpDeliveryResponse.builder()
                .channel(
                        result.getChannelUsed()
                )
                .expirySeconds(
                        expirySeconds
                )
                .requestId(
                        requestId
                )
                .build();
    }

    @Override
    public void pingRequest() {

        repository.count();

        cacheManager.ping();

        channelDeliveryRouter.ping();
    }

    @Override
    public void pingVerification() {

        repository.count();

        cacheManager.ping();
    }

    private void sendWebhookIfConfigured(
            String clientId,
            OtpRequest otpRequest,
            WebhookEventType eventType
    ) {

        DeveloperApplication app =
                appRepository
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
                    eventType,
                    otpRequest
            );

        } catch (Exception e) {

            log.error(
                    "Webhook dispatch failed clientId={}",
                    clientId,
                    e
            );
        }
    }

    private void validateApplicationAccess(
            String clientId
    ) {

        if (clientId == null) {

            return;
        }

        DeveloperApplication app =
                appRepository.findByClientId(clientId)
                        .orElseThrow(
                                () -> new ForbiddenOperationException(
                                        "Application not found"
                                )
                        );

        if (app.getStatus() == ApplicationStatus.REVOKED) {

            throw new ForbiddenOperationException(
                    "Application revoked"
            );
        }

        if (app.getStatus() == ApplicationStatus.SUSPENDED_BY_ADMIN) {

            throw new ForbiddenOperationException(
                    "Application suspended"
            );
        }

        if (app.getStatus() == ApplicationStatus.DISABLED_BY_OWNER) {

            throw new ForbiddenOperationException(
                    "Application disabled by owner"
            );
        }
    }

    private void validateDailyLimit(
            String identifier,
            String clientId
    ) {

        if (clientId == null) {

            return;
        }

        long todayUsage =
                otpUsageService.getTodayUsage(
                        clientId
                );

        if (todayUsage >= DAILY_LIMIT) {

            throw new RateLimitExceededException(
                    "Daily OTP limit exceeded"
            );
        }
    }

    private void validatePerMinuteLimit(
            String identifier
    ) {

        long recentCount =
                repository.countByIdentifierAndCreatedAtAfter(
                        identifier,
                        LocalDateTime.now()
                                .minusMinutes(1)
                );

        if (recentCount >= PER_MINUTE_LIMIT) {

            throw new RateLimitExceededException(
                    "Too many OTP requests"
            );
        }
    }

    private void validateApplicationPerMinuteLimit(
            String applicationName
    ) {

        long recentCount =
                repository
                        .countByApplicationNameAndCreatedAtAfter(
                                applicationName,
                                LocalDateTime.now()
                                        .minusMinutes(1)
                        );

        if (recentCount
                >= APPLICATION_PER_MINUTE_LIMIT) {

            throw new RateLimitExceededException(
                    "Application OTP rate limit exceeded"
            );
        }
    }

    private OtpRequest createOtpRequest(
            OtpRequestDTO request,
            String hash
    ) {

        OtpRequest otpRequest =
                new OtpRequest();

        otpRequest.setIdentifier(
                request.getIdentifier()
        );

        otpRequest.setOtpHash(
                hash
        );

        otpRequest.setOtpType(
                OtpType.APPLICATION
        );

        otpRequest.setPurpose(
                request.getPurpose()
        );

        otpRequest.setApplicationName(
                request.getApplicationName()
        );

        otpRequest.setDisplayName(
                request.getDisplayName()
        );

        otpRequest.setStatus(
                OtpStatus.CREATED
        );

        otpRequest.setCreatedAt(
                LocalDateTime.now()
        );

        otpRequest.setExpiresAt(
                LocalDateTime.now()
                        .plusSeconds(expirySeconds)
        );

        otpRequest.setAttemptCount(
                0
        );

        return repository.save(
                otpRequest
        );
    }

    private OtpDeliveryResult deliverOtp(
            OtpDeliveryContext context
    ) {

        try {

            return channelDeliveryRouter.deliver(
                    context
            );

        } catch (Exception e) {

            log.error(
                    "OTP delivery infrastructure failure identifier={}",
                    context.getIdentifier(),
                    e
            );

            throw new OtpException(
                    "OTP delivery failed"
            );
        }
    }
}