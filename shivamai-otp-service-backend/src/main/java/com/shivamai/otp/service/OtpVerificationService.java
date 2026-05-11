package com.shivamai.otp.service;

import com.shivamai.otp.cache.CachedOtp;
import com.shivamai.otp.cache.OtpCacheManager;
import com.shivamai.otp.entity.OtpRequest;
import com.shivamai.otp.enums.OtpStatus;
import com.shivamai.otp.exception.OtpVerificationException;
import com.shivamai.otp.repository.DeveloperAppRepository;
import com.shivamai.otp.repository.OtpRequestRepository;
import com.shivamai.otp.util.OtpHashUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpVerificationService {

    private static final int MAX_ATTEMPTS = 3;

    private final OtpRequestRepository repository;
    private final OtpCacheManager cacheManager;
    private final UsageService usageService;
    private final WebHookService webhookService;
    private final DeveloperAppRepository developerAppRepository;
    private final ApiAccessLogService apiAccessLogService;

    public boolean verifyOtp(String identifier, Long requestId, String otp) {

        log.debug("Verifying OTP for identifier={}, requestId={}", identifier, requestId);

        if (requestId == null) {
            throw new OtpVerificationException("RequestId is required");
        }

        if (otp == null || otp.isBlank()) {
            throw new OtpVerificationException("OTP is required");
        }

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        String clientId = null;
        String ip = "UNKNOWN";

        if (attributes != null) {
            clientId = (String) attributes.getAttribute(
                    "clientId",
                    RequestAttributes.SCOPE_REQUEST
            );

            try {
                HttpServletRequest request =
                        ((ServletRequestAttributes) attributes).getRequest();
                ip = request.getRemoteAddr();
            }
            catch (Exception ignored) {}
        }

        OtpRequest request = repository.findById(requestId)
                .orElseThrow(() -> new OtpVerificationException("OTP request not found"));

        if (!request.getIdentifier().equals(identifier)) {
            throw new OtpVerificationException("Identifier mismatch");
        }

        String key = identifier + ":" + requestId;

        synchronized (key) {
            if (request.getStatus() != OtpStatus.DELIVERED) {
                throw new OtpVerificationException("OTP already used or invalid");
            }

            CachedOtp cached = cacheManager.getOtp(identifier, requestId);

            if (cached == null) {
                request.setStatus(OtpStatus.EXPIRED);
                repository.save(request);

                if (clientId != null) {
                    apiAccessLogService.logEvent(
                            clientId,
                            identifier,
                            "/otp/verify",
                            "OTP_EXPIRED",
                            ip,
                            400
                    );
                }
                throw new OtpVerificationException("OTP expired or not found");
            }

            if (System.currentTimeMillis() >= cached.getExpiryTime()) {

                request.setStatus(OtpStatus.EXPIRED);
                repository.save(request);

                cacheManager.removeOtp(identifier, requestId);

                if (clientId != null) {
                    apiAccessLogService.logEvent(
                            clientId,
                            identifier,
                            "/otp/verify",
                            "OTP_EXPIRED",
                            ip,
                            400
                    );
                }

                throw new OtpVerificationException("OTP expired");
            }

            int attempts = request.getAttemptCount() + 1;
            request.setAttemptCount(attempts);

            if (attempts > MAX_ATTEMPTS) {

                request.setStatus(OtpStatus.BLOCKED);
                repository.save(request);

                cacheManager.removeOtp(identifier, requestId);

                log.warn("OTP blocked for identifier={}", identifier);

                if (clientId != null) {
                    apiAccessLogService.logEvent(
                            clientId,
                            identifier,
                            "/otp/verify",
                            "OTP_BLOCKED",
                            ip,
                            400
                    );
                }

                throw new OtpVerificationException(
                        "Maximum attempts exceeded. Request new OTP."
                );
            }

            String hash = OtpHashUtil.hash(otp);

            if (hash.equals(cached.getOtpHash())) {
                cacheManager.removeOtp(identifier, requestId);
                request.setStatus(OtpStatus.VERIFIED);
//                request.setVerifiedAt(LocalDateTime.now());
                repository.save(request);
                log.info("OTP verified successfully for identifier={}", identifier);

                if (clientId != null) {

                    usageService.recordOtpVerification(clientId);
                    apiAccessLogService.logApiAccess(
                            clientId,
                            identifier,
                            "/otp/verify",
                            ip,
                            200
                    );
                    developerAppRepository.findByClientId(clientId)
                            .ifPresent(app -> {
                                if (app.getWebhookUrl() != null && !app.getWebhookUrl().isBlank()) {
                                    webhookService.sendVerificationEvent(
                                            app.getWebhookUrl(),
                                            request
                                    );
                                }
                            });
                }
                return true;
            }

            repository.save(request);
            log.warn("Invalid OTP attempt for identifier={}, attempts={}", identifier, attempts);

            int remaining = Math.max(0, MAX_ATTEMPTS - attempts);
            if (clientId != null) {
                apiAccessLogService.logEvent(
                        clientId,
                        identifier,
                        "/otp/verify",
                        "INVALID_OTP",
                        ip,
                        400
                );
            }

            throw new OtpVerificationException(
                    "Invalid OTP. Attempts remaining: " + remaining
            );
        }
    }

    public boolean verifyOtp(String identifier, String otp) {

        OtpRequest request = repository
                .findTopByIdentifierAndStatusInAndExpiresAtAfterOrderByCreatedAtDesc(
                        identifier,
                        List.of(OtpStatus.DELIVERED),
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new OtpVerificationException("No active OTP request"));
        return verifyOtp(identifier, request.getId(), otp);
    }

    public void ping() {
        repository.count();
    }
}