package com.shivamai.otp.account.service;

import com.shivamai.otp.account.entity.DeveloperAccount;
import com.shivamai.otp.application.entity.DeveloperApplication;

import com.shivamai.otp.common.exception.*;

import com.shivamai.otp.common.pagination.PageQuery;
import com.shivamai.otp.common.pagination.PageableFactory;
import com.shivamai.otp.common.security.ApplicationSessionInvalidationService;

import com.shivamai.otp.common.security.JwtBlacklistService;
import com.shivamai.otp.common.security.JwtUtil;

import com.shivamai.otp.account.repository.DeveloperAccountRepository;
import com.shivamai.otp.application.repository.DeveloperApplicationRepository;

import com.shivamai.otp.audit.service.AuditService;
import com.shivamai.otp.notification.service.NotificationService;

import com.shivamai.otp.otp.repository.OtpRequestRepository;
import com.shivamai.otp.usage.service.OtpUsageService;
import com.shivamai.otp.webhook.dto.ApplicationWebhookLogResponse;
import com.shivamai.otp.webhook.entity.WebhookLog;
import com.shivamai.otp.webhook.enums.WebhookEventType;
import com.shivamai.otp.webhook.enums.WebhookStatus;
import com.shivamai.otp.webhook.repository.WebhookLogRepository;
import com.shivamai.otp.webhook.specification.DeveloperWebhookLogSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeveloperWebhookServiceImpl
        implements DeveloperWebhookService {

    private final DeveloperAccountRepository developerRepository;

    private final DeveloperApplicationRepository appRepository;

    private final PasswordEncoder passwordEncoder;

    private final OtpUsageService otpUsageService;

    private final OtpRequestRepository otpRequestRepository;

    private final NotificationService notificationService;

    private final WebhookLogRepository webhookLogRepository;

    private final AuditService auditService;

    private final ApplicationSessionInvalidationService sessionInvalidationService;

    private final JwtUtil jwtUtil;

    private final JwtBlacklistService jwtBlacklistService;

    @Override
    public Page<ApplicationWebhookLogResponse> getWebhookLogs(
            Long appId,
            int page,
            int size,
            String sortBy,
            String direction,
            String search,
            List<WebhookStatus> statuses,
            List<WebhookEventType> eventTypes,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        DeveloperAccount developer =
                getCurrentDeveloper();

        DeveloperApplication app =
                getOwnedApp(
                        appId,
                        developer
                );

        PageQuery query =
                PageQuery.builder()
                        .page(page)
                        .size(size)
                        .sortBy(sortBy)
                        .direction(direction)
                        .search(search)
                        .build();

        Pageable pageable =
                PageableFactory.create(
                        query
                );

        Page<WebhookLog> logs =
                webhookLogRepository.findAll(
                        DeveloperWebhookLogSpecification.withFilters(
                                app.getClientId(),
                                search,
                                statuses,
                                eventTypes,
                                fromDate,
                                toDate
                        ),
                        pageable
                );

        return logs.map(
                log ->
                        ApplicationWebhookLogResponse
                                .builder()
                                .id(
                                        log.getId()
                                )
                                .eventType(
                                        log.getEventType()
                                )
                                .status(
                                        log.getStatus()
                                )
                                .responseStatusCode(
                                        log.getResponseStatusCode()
                                )
                                .retryCount(
                                        log.getRetryCount()
                                )
                                .failureReason(
                                        log.getFailureReason()
                                )
                                .deliveredAt(
                                        log.getDeliveredAt()
                                )
                                .createdAt(
                                        log.getCreatedAt()
                                )
                                .build()
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

    private DeveloperApplication getOwnedApp(Long appId, DeveloperAccount developer) {

        DeveloperApplication app =
                appRepository.findByIdAndDeveloperId(appId, developer.getId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Application not found"
                                )
                        );

        if (!app.getDeveloper().getId().equals(developer.getId())) {

            throw new ForbiddenOperationException(
                    "You do not own this application"
            );
        }

        return app;
    }
}