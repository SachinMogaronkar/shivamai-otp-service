package com.shivamai.otp.account.service;

import com.shivamai.otp.account.dto.request.DeleteAccountRequest;
import com.shivamai.otp.account.dto.response.DeveloperDashboardMetricsResponse;
import com.shivamai.otp.account.dto.response.DeveloperProfileResponse;

import com.shivamai.otp.account.entity.DeveloperAccount;
import com.shivamai.otp.application.entity.DeveloperApplication;

import com.shivamai.otp.account.enums.DeveloperAccountStatus;
import com.shivamai.otp.application.enums.ApplicationStatus;

import com.shivamai.otp.audit.enums.AuditEventType;

import com.shivamai.otp.common.exception.*;

import com.shivamai.otp.common.security.JwtBlacklistService;
import com.shivamai.otp.common.security.JwtUtil;

import com.shivamai.otp.account.repository.DeveloperAccountRepository;
import com.shivamai.otp.application.repository.DeveloperApplicationRepository;

import com.shivamai.otp.audit.service.AuditService;

import com.shivamai.otp.otp.enums.OtpStatus;
import com.shivamai.otp.otp.repository.OtpRequestRepository;
import com.shivamai.otp.usage.service.OtpUsageService;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeveloperAccountServiceImpl
        implements DeveloperAccountService {

    private final DeveloperAccountRepository developerRepository;

    private final DeveloperApplicationRepository appRepository;

    private final PasswordEncoder passwordEncoder;

    private final OtpUsageService otpUsageService;

    private final OtpRequestRepository otpRequestRepository;

    private final AuditService auditService;

    private final JwtUtil jwtUtil;

    private final JwtBlacklistService jwtBlacklistService;

    // =====================================
    // PROFILE
    // =====================================

    @Override
    @Transactional(readOnly = true)
    public DeveloperProfileResponse getProfile() {

        DeveloperAccount developer =
                getCurrentDeveloper();

        validateDeveloperAccess(
                developer
        );

        log.info(
                "Fetching profile for developer={}",
                developer.getIdentifier()
        );

        return DeveloperProfileResponse.builder()
                .identifier(
                        developer.getIdentifier()
                )
                .status(
                        developer.getStatus()
                )
                .createdAt(
                        developer.getCreatedAt()
                )
                .build();
    }

    // =====================================
    // METRICS
    // =====================================

    @Override
    public DeveloperDashboardMetricsResponse getMetrics() {

        DeveloperAccount developer =
                getCurrentDeveloper();

        long totalRequests =
                otpRequestRepository.count();

        long verified =
                otpRequestRepository.countByStatus(
                        OtpStatus.VERIFIED
                );

        long failed =
                otpRequestRepository.countByStatusIn(
                        List.of(
                                OtpStatus.BLOCKED,
                                OtpStatus.EXPIRED,
                                OtpStatus.DELIVERY_FAILED
                        )
                );

        long activeApps =
                appRepository
                        .countByDeveloperIdAndStatus(
                                developer.getId(),
                                ApplicationStatus.ACTIVE
                        );

        long todayUsage =
                otpUsageService.getTodayUsage(
                        developer.getIdentifier()
                );

        double successRate =
                totalRequests == 0
                        ? 0
                        : ((double) verified / totalRequests) * 100;

        return DeveloperDashboardMetricsResponse
                .builder()
                .totalOtpRequests(
                        totalRequests
                )
                .successfulVerifications(
                        verified
                )
                .failedVerifications(
                        failed
                )
                .activeApplications(
                        activeApps
                )
                .todayUsage(
                        todayUsage
                )
                .successRate(
                        Math.round(successRate * 100.0) / 100.0
                )
                .build();
    }

    // =====================================
    // DELETE ACCOUNT
    // =====================================

    @Override
    public void deleteAccount(DeleteAccountRequest request, HttpServletRequest httpRequest) {

        DeveloperAccount developer = getCurrentDeveloper();

        if (!passwordEncoder.matches(
                request.getPassword(),
                developer.getPasswordHash()
        )) {

            auditService.logDeveloperEvent(
                    "developer-dashboard",
                    developer.getIdentifier(),
                    "/developer/dashboard/account",
                    AuditEventType.ACCOUNT_DELETION_FAILED,
                    401
            );

            throw new InvalidRequestException("Invalid password");

        }

        List<DeveloperApplication> applications =
                appRepository.findByDeveloperId(
                        developer.getId()
                );

        boolean hasRestrictedApps =
                applications.stream().anyMatch(
                        app ->
                                app.getStatus()
                                        == ApplicationStatus.REVOKED
                                        || app.getStatus()
                                        == ApplicationStatus.SUSPENDED_BY_ADMIN
                );

        if (hasRestrictedApps) {

            throw new InvalidRequestException(
                    "Account cannot be deleted while suspended/revoked applications exist"
            );
        }

        for (DeveloperApplication app : applications) {

            appRepository.delete(app);

            auditService.logDeveloperEvent(
                    app.getClientId(),
                    developer.getIdentifier(),
                    "/developer/dashboard/account",
                    AuditEventType.APP_DELETED_BY_DEVELOPER,
                    200
            );

            log.info(
                    "Application deleted by developer={}",
                    app.getClientId()
            );
        }

        developerRepository.delete(developer);

        auditService.logDeveloperEvent(
                "developer-dashboard",
                developer.getIdentifier(),
                "/developer/dashboard/account",
                AuditEventType.ACCOUNT_DELETED_BY_DEVELOPER,
                200
        );

        String token = httpRequest.getHeader("Authorization").substring(7);

        long expiry = jwtUtil.extractExpiration(token).getTime();

        jwtBlacklistService.blacklist(token, expiry);

        SecurityContextHolder.clearContext();

        log.info(
                "Developer account deleted={}",
                developer.getIdentifier()
        );
    }

    // =====================================
    // INTERNAL HELPERS
    // =====================================

    private DeveloperAccount getCurrentDeveloper() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }

        String identifier = authentication.getName();

        return developerRepository
                .findByIdentifier(identifier)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Developer not found"
                        )
                );
    }

    private void validateDeveloperAccess(DeveloperAccount developer) {

        if (developer.getStatus() == DeveloperAccountStatus.SUSPENDED_BY_ADMIN) {

            throw new ForbiddenOperationException("Developer account suspended by admin");

        }

        if (developer.getStatus() == DeveloperAccountStatus.REVOKED) {

            throw new ForbiddenOperationException("Developer account revoked");

        }

        if (developer.getStatus() != DeveloperAccountStatus.ACTIVE) {

            throw new ForbiddenOperationException("Developer account is not active");

        }
    }
}