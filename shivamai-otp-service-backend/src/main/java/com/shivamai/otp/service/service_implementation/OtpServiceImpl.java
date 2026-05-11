package com.shivamai.otp.service.service_implementation;

import com.shivamai.otp.cache.CachedOtp;
import com.shivamai.otp.cache.OtpCacheManager;
import com.shivamai.otp.channel.ChannelRouter;
import com.shivamai.otp.channel.DeliveryResult;
import com.shivamai.otp.dtoresponse.OtpResponse;
import com.shivamai.otp.entity.OtpRequest;
import com.shivamai.otp.enums.OtpStatus;
import com.shivamai.otp.enums.OtpType;
import com.shivamai.otp.exception.OtpException;
import com.shivamai.otp.exception.RateLimitExceededException;
import com.shivamai.otp.repository.OtpRequestRepository;
import com.shivamai.otp.service.ApiAccessLogService;
import com.shivamai.otp.service.OtpService;
import com.shivamai.otp.service.UsageService;
import com.shivamai.otp.util.OtpGenerator;
import com.shivamai.otp.util.OtpHashUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OtpServiceImpl implements OtpService {

    @Autowired
    private OtpRequestRepository repository;

    @Autowired
    private OtpCacheManager cacheManager;

    @Autowired
    private ChannelRouter channelRouter;

    @Autowired
    private UsageService usageService;

    @Autowired
    private ApiAccessLogService apiAccessLogService;

    @Value("${otp.expiry.seconds}")
    private int expirySeconds;

    private static final Logger log = LoggerFactory.getLogger(OtpServiceImpl.class);

    @Override
    public OtpResponse requestOtp(String identifier, OtpType type) {

        log.info("OTP request initiated for identifier: {}, type: {}", identifier, type);

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        String clientId = null;
        String ip = "UNKNOWN";

        if (attributes != null) {
            clientId = (String) attributes.getAttribute("clientId", RequestAttributes.SCOPE_REQUEST);

            try {
                HttpServletRequest request =
                        ((ServletRequestAttributes) attributes).getRequest();
                ip = request.getRemoteAddr();
            } catch (Exception ignored) {}
        }

        try {

            // 🔹 Daily limit
            if (clientId != null) {
                long todayUsage = usageService.getTodayUsage(clientId);
                if (todayUsage >= 100) {
                    throw new RateLimitExceededException("Daily OTP limit exceeded");
                }
            }

            // 🔹 Per-minute limit
            long recentCount = repository.countByIdentifierAndCreatedAtAfter(
                    identifier,
                    LocalDateTime.now().minusMinutes(1)
            );

            if (recentCount >= 20) {
                throw new RateLimitExceededException("Too many OTP requests. Try again later.");
            }

            // 🔹 Reuse existing
            Optional<OtpRequest> existing =
                    repository.findTopByIdentifierAndStatusInAndExpiresAtAfterOrderByCreatedAtDesc(
                            identifier,
                            List.of(OtpStatus.DELIVERED),
                            LocalDateTime.now()
                    );

            if (existing.isPresent()) {

                OtpRequest request = existing.get();
                CachedOtp cached = cacheManager.getOtp(identifier, request.getId());

                if (cached != null) {

                    int remaining =
                            (int) ((cached.getExpiryTime() - System.currentTimeMillis()) / 1000);

                    return OtpResponse.builder()
                            .channel(request.getChannel())
                            .expirySeconds(Math.max(remaining, 0))
                            .requestId(request.getId())
                            .build();
                }
            }

            // 🔹 Generate OTP
            String otp = OtpGenerator.generateOtp();
            String hash = OtpHashUtil.hash(otp);

            OtpRequest request = new OtpRequest();
            request.setIdentifier(identifier);
            request.setOtpHash(hash);
            request.setOtpType(type);
            request.setStatus(OtpStatus.CREATED);
            request.setCreatedAt(LocalDateTime.now());
            request.setExpiresAt(LocalDateTime.now().plusSeconds(expirySeconds));
            request.setAttemptCount(0);

            repository.save(request);

            cacheManager.storeOtp(
                    identifier,
                    request.getId(),
                    otp,
                    hash,
                    System.currentTimeMillis() + expirySeconds * 1000
            );

            DeliveryResult result =
                    channelRouter.deliver(identifier, otp, type, expirySeconds);

            if (!result.isSuccess()) {
                throw new OtpException("OTP delivery failed");
            }

            request.setChannel(result.getChannelUsed());
            request.setStatus(OtpStatus.DELIVERED);
            repository.save(request);

            if (clientId != null) {
                usageService.recordOtpRequest(clientId);

                apiAccessLogService.logApiAccess(
                        clientId,
                        identifier,
                        "/otp/request",
                        ip,
                        200
                );
            }

            return OtpResponse.builder()
                    .channel(result.getChannelUsed())
                    .expirySeconds(expirySeconds)
                    .requestId(request.getId())
                    .build();

        } catch (Exception e) {

            if (clientId != null) {
                apiAccessLogService.logEvent(
                        clientId,
                        identifier,
                        "/otp/request",
                        "OTP_REQUEST_FAILED",
                        ip,
                        400
                );
            }

            throw e;
        }
    }

    @Override
    public OtpResponse resendOtp(String identifier, Long requestId) {

        OtpRequest request = repository.findById(requestId)
                .orElseThrow(() -> new OtpException("OTP request not found"));

        if (!request.getIdentifier().equals(identifier)) {
            throw new OtpException("Identifier mismatch");
        }

        if (request.getStatus() != OtpStatus.DELIVERED) {
            throw new OtpException("OTP cannot be resent");
        }

        CachedOtp cached = cacheManager.getOtp(identifier, requestId);

        if (cached == null) {
            throw new OtpException("OTP expired. Request new OTP.");
        }

        String otp = cached.getOtp();

        DeliveryResult result =
                channelRouter.deliver(identifier, otp, request.getOtpType(), expirySeconds);

        if (!result.isSuccess()) {
            throw new OtpException("OTP delivery failed");
        }

        request.setLastResendAt(LocalDateTime.now());
        repository.save(request);

        int remaining =
                (int) ((cached.getExpiryTime() - System.currentTimeMillis()) / 1000);

        return OtpResponse.builder()
                .channel(result.getChannelUsed())
                .expirySeconds(Math.max(remaining, 0))
                .requestId(requestId)
                .build();
    }

    public void pingRequest() {
        repository.count();
        cacheManager.ping();
        channelRouter.ping();
    }

    public void pingVerification() {
        repository.count();
        cacheManager.ping();
    }
}