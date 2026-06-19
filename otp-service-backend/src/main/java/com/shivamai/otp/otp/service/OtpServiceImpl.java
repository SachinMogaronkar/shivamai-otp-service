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
import com.shivamai.otp.otp.dto.OtpTemplateContent;
import com.shivamai.otp.otp.dto.request.OtpRequestDTO;
import com.shivamai.otp.otp.dto.response.OtpDeliveryResponse;

import com.shivamai.otp.otp.entity.OtpRequest;

import com.shivamai.otp.otp.enums.OtpChannelType;
import com.shivamai.otp.otp.enums.OtpStatus;
import com.shivamai.otp.otp.enums.OtpType;

import com.shivamai.otp.otp.repository.OtpRequestRepository;

import com.shivamai.otp.otp.resolver.OtpTemplateContentResolver;
import com.shivamai.otp.usage.service.OtpUsageService;

import com.shivamai.otp.webhook.enums.WebhookEventType;
import com.shivamai.otp.webhook.service.WebHookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.time.Instant;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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

    private final ConcurrentHashMap<String, Instant> resendCooldown =
            new ConcurrentHashMap<>();

    private static final long RESEND_COOLDOWN_SECONDS = 30;

    private final OtpRequestRepository repository;

    private final OtpSessionStore cacheManager;

    private final ChannelDeliveryRouter channelDeliveryRouter;

    private final OtpTemplateContentResolver templateResolver;

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

        String cooldownKey =
                identifier
                        + ":"
                        + request.getPurpose();

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

                OtpChannelType channel =
                        existingRequest.getChannel();

                if (canResend(cooldownKey)) {

                    OtpDeliveryResult reusedDelivery =
                            deliverOtp(
                                    buildDeliveryContext(
                                            cached.getOtp(),
                                            existingRequest
                                    )
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

                    markResend(cooldownKey);

                    channel = reusedDelivery.getChannelUsed();
                }

                return OtpDeliveryResponse.builder()
                        .channel(channel)
                        .requestId(existingRequest.getId())
                        .expiresAt(existingRequest.getExpiresAt())
                        .remainingSeconds(
                                calculateRemainingSeconds(
                                        existingRequest.getExpiresAt()
                                )
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
                        buildDeliveryContext(
                                otp,
                                otpRequest
                        )
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

        markResend(cooldownKey);

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
                .channel(result.getChannelUsed())
                .requestId(otpRequest.getId())
                .expiresAt(otpRequest.getExpiresAt())
                .remainingSeconds(
                        calculateRemainingSeconds(
                                otpRequest.getExpiresAt()
                        )
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

        String cooldownKey =
                identifier + ":" + otpRequest.getPurpose();

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

        if (!canResend(cooldownKey)) {

            return OtpDeliveryResponse.builder()
                    .channel(otpRequest.getChannel())
                    .requestId(requestId)
                    .expiresAt(otpRequest.getExpiresAt())
                    .remainingSeconds(
                            calculateRemainingSeconds(
                                    otpRequest.getExpiresAt()
                            )
                    )
                    .build();
        }

        OtpDeliveryResult result =
                deliverOtp(
                        buildDeliveryContext(
                                cached.getOtp(),
                                otpRequest
                        )
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

        markResend(cooldownKey);

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
                .channel(result.getChannelUsed())
                .requestId(requestId)
                .expiresAt(otpRequest.getExpiresAt())
                .remainingSeconds(
                        calculateRemainingSeconds(
                                otpRequest.getExpiresAt()
                        )
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

    private long calculateRemainingSeconds(
            LocalDateTime expiresAt
    ) {

        return Math.max(
                0,
                java.time.Duration.between(
                        LocalDateTime.now(),
                        expiresAt
                ).toSeconds()
        );
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

    private boolean canResend(String cooldownKey) {

        Instant lastSent =
                resendCooldown.get(cooldownKey);

        if (lastSent == null) {
            return true;
        }

        return Instant.now()
                .isAfter(
                        lastSent.plusSeconds(
                                RESEND_COOLDOWN_SECONDS
                        )
                );
    }

    private void markResend(String cooldownKey) {

        resendCooldown.put(
                cooldownKey,
                Instant.now()
        );
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

    private OtpDeliveryContext buildDeliveryContext(
            String otp,
            OtpRequest otpRequest
    ) {

        OtpTemplateContent templateContent =
                templateResolver.resolve(
                        otpRequest.getPurpose(),
                        otpRequest.getApplicationName()
                );

        Context thymeleafContext =
                new Context();

        thymeleafContext.setVariable(
                "otp",
                otp
        );

        thymeleafContext.setVariable(
                "expiryMinutes",
                expirySeconds / 60
        );

        return OtpDeliveryContext.builder()
                .identifier(
                        otpRequest.getIdentifier()
                )
                .channelType(
                        OtpChannelType.EMAIL
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
                .subject(
                        templateContent.getSubject()
                )
                .template(
                        templateContent.getTemplate()
                )
                .context(
                        thymeleafContext
                )
                .build();
    }
}