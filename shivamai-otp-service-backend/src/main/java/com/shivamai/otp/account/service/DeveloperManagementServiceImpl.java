package com.shivamai.otp.account.service;

import com.shivamai.otp.account.dto.response.DeveloperAccountResponse;
import com.shivamai.otp.account.entity.DeveloperAccount;
import com.shivamai.otp.account.enums.DeveloperAccountStatus;
import com.shivamai.otp.account.repository.DeveloperAccountRepository;

import com.shivamai.otp.account.specification.DeveloperAccountSpecification;
import com.shivamai.otp.account.specification.DeveloperApplicationAdminSpecification;
import com.shivamai.otp.application.dto.response.ApplicationDetailResponse;
import com.shivamai.otp.application.entity.DeveloperApplication;
import com.shivamai.otp.application.enums.ApplicationStatus;
import com.shivamai.otp.application.repository.DeveloperApplicationRepository;

import com.shivamai.otp.audit.enums.AuditEventType;
import com.shivamai.otp.audit.service.AuditService;

import com.shivamai.otp.common.exception.InvalidRequestException;
import com.shivamai.otp.common.exception.ResourceNotFoundException;

import com.shivamai.otp.common.pagination.PageQuery;
import com.shivamai.otp.common.pagination.PageableFactory;
import com.shivamai.otp.notification.service.NotificationService;

import com.shivamai.otp.webhook.dto.WebhookLogResponse;
import com.shivamai.otp.webhook.entity.WebhookLog;
import com.shivamai.otp.webhook.enums.WebhookEventType;
import com.shivamai.otp.webhook.enums.WebhookStatus;
import com.shivamai.otp.webhook.repository.WebhookLogRepository;

import com.shivamai.otp.webhook.specification.AdminWebhookLogSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeveloperManagementServiceImpl
        implements DeveloperManagementService {

    private final DeveloperAccountRepository developerRepository;

    private final DeveloperApplicationRepository appRepository;

    private final WebhookLogRepository webhookLogRepository;

    private final NotificationService notificationService;

    private final AuditService auditService;

    // =====================================
    // GET PENDING DEVELOPERS
    // =====================================

    @Override
    @Transactional(readOnly = true)
    public Page<DeveloperAccountResponse> getPendingDevelopers(
            PageQuery query,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {

            throw new InvalidRequestException(
                    "fromDate cannot be after toDate"
            );
        }

        Pageable pageable =
                PageableFactory.create(
                        query
                );

        Page<DeveloperAccount> developers =
                developerRepository.findAll(
                        DeveloperAccountSpecification
                                .withFilters(
                                        query.getSearch(),
                                        List.of(
                                                DeveloperAccountStatus.PENDING_ADMIN_APPROVAL
                                        ),
                                        fromDate,
                                        toDate
                                ),
                        pageable
                );

        log.info(
                "Fetching pending developers page={} size={}",
                query.getPage(),
                query.getSize()
        );

        return developers.map(
                this::mapDeveloper
        );
    }
    // =====================================
    // GET DEVELOPERS
    // =====================================

    @Override
    @Transactional(readOnly = true)
    public Page<DeveloperAccountResponse>
    getDevelopers(
            PageQuery query,
            List<DeveloperAccountStatus> statuses,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {

            throw new InvalidRequestException(
                    "fromDate cannot be after toDate"
            );
        }

        Pageable pageable =
                PageableFactory.create(
                        query
                );

        Page<DeveloperAccount> developers =
                developerRepository.findAll(
                        DeveloperAccountSpecification
                                .withFilters(
                                        query.getSearch(),
                                        statuses,
                                        fromDate,
                                        toDate
                                ),
                        pageable
                );

        log.info(
                "Fetching developers page={} size={}",
                query.getPage(),
                query.getSize()
        );

        return developers.map(
                this::mapDeveloper
        );
    }

    // =====================================
    // GET DEVELOPER
    // =====================================

    @Override
    public DeveloperAccountResponse getDeveloper(Long id) {

        log.info(
                "Fetching developer id={}",
                id
        );

        return mapDeveloper(
                getDeveloperOrThrow(id)
        );
    }

    // =====================================
    // APPROVE DEVELOPER
    // =====================================

    @Override
    public DeveloperAccountResponse approveDeveloper(Long id) {

        DeveloperAccount developer =
                getDeveloperOrThrow(id);

        if (developer.getStatus()
                != DeveloperAccountStatus.PENDING_ADMIN_APPROVAL) {

            throw new InvalidRequestException(
                    "Developer approval not allowed"
            );
        }

        developer.setStatus(
                DeveloperAccountStatus.ACTIVE
        );

        DeveloperAccount updated =
                developerRepository.save(developer);

        notificationService
                .sendDeveloperApprovedNotification(
                        updated.getIdentifier()
                );

        notificationService
                .sendDeveloperWelcomeNotification(
                        updated.getIdentifier()
                );

        auditService.logAdminEvent(
                updated.getIdentifier(),
                "/admin/developers/" + id + "/approve",
                AuditEventType.DEVELOPER_APPROVED,
                200
        );

        log.info(
                "Developer approved={}",
                updated.getIdentifier()
        );

        return mapDeveloper(updated);
    }

    // =====================================
    // SUSPEND DEVELOPER
    // =====================================

    @Override
    public DeveloperAccountResponse suspendDeveloper(Long id) {

        DeveloperAccount developer =
                getDeveloperOrThrow(id);

        if (developer.getStatus()
                == DeveloperAccountStatus.SUSPENDED_BY_ADMIN) {

            throw new InvalidRequestException(
                    "Developer already suspended"
            );
        }

        if (developer.getStatus()
                == DeveloperAccountStatus.REVOKED) {

            throw new InvalidRequestException(
                    "Revoked developer cannot be suspended"
            );
        }

        developer.setStatus(
                DeveloperAccountStatus.SUSPENDED_BY_ADMIN
        );

        DeveloperAccount updated =
                developerRepository.save(developer);

        notificationService
                .sendDeveloperSuspendedNotification(
                        updated.getIdentifier()
                );

        auditService.logAdminEvent(
                updated.getIdentifier(),
                "/admin/developers/" + id + "/suspend",
                AuditEventType.DEVELOPER_SUSPENDED,
                200
        );

        log.info(
                "Developer suspended={}",
                updated.getIdentifier()
        );

        return mapDeveloper(updated);
    }

    // =====================================
    // ACTIVATE DEVELOPER
    // =====================================

    @Override
    public DeveloperAccountResponse activateDeveloper(Long id) {

        DeveloperAccount developer =
                getDeveloperOrThrow(id);

        if (developer.getStatus()
                != DeveloperAccountStatus.SUSPENDED_BY_ADMIN) {

            throw new InvalidRequestException(
                    "Only suspended developers can be activated"
            );
        }

        developer.setStatus(
                DeveloperAccountStatus.ACTIVE
        );

        DeveloperAccount updated =
                developerRepository.save(developer);

        notificationService
                .sendDeveloperReactivatedNotification(
                        updated.getIdentifier()
                );

        auditService.logAdminEvent(
                updated.getIdentifier(),
                "/admin/developers/" + id + "/activate",
                AuditEventType.DEVELOPER_REACTIVATED,
                200
        );

        log.info(
                "Developer reactivated={}",
                updated.getIdentifier()
        );

        return mapDeveloper(updated);
    }

    // =====================================
    // REVOKE DEVELOPER
    // =====================================

    @Override
    public DeveloperAccountResponse revokeDeveloper(Long id) {

        DeveloperAccount developer =
                getDeveloperOrThrow(id);

        if (developer.getStatus()
                == DeveloperAccountStatus.REVOKED) {

            throw new InvalidRequestException(
                    "Developer already revoked"
            );
        }

        developer.setStatus(
                DeveloperAccountStatus.REVOKED
        );

        List<DeveloperApplication> applications =
                appRepository.findByDeveloperId(
                        developer.getId()
                );

        for (DeveloperApplication application
                : applications) {

            application.setStatus(
                    ApplicationStatus.REVOKED
            );

            auditService.logAdminEvent(
                    application.getClientId(),
                    "/admin/developers/" + id + "/revoke",
                    AuditEventType.APP_REVOKED_BY_ADMIN,
                    200
            );
        }

        appRepository.saveAll(applications
        );

        DeveloperAccount updated = developerRepository.save(developer);

        notificationService.sendDeveloperRevokedNotification(
                updated.getIdentifier()
        );

        auditService.logAdminEvent(
                updated.getIdentifier(),
                "/admin/developers/" + id + "/revoke",
                AuditEventType.DEVELOPER_REVOKED_BY_ADMIN,
                200
        );

        log.info(
                "Developer revoked={}",
                updated.getIdentifier()
        );

        return mapDeveloper(updated);
    }

    // =====================================
    // GET ALL APPS
    // =====================================

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationDetailResponse>
    getAllApps(
            PageQuery query,
            List<ApplicationStatus> statuses,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {

            throw new InvalidRequestException(
                    "fromDate cannot be after toDate"
            );
        }

        Pageable pageable =
                PageableFactory.create(
                        query
                );

        Page<DeveloperApplication> applications =
                appRepository.findAll(
                        DeveloperApplicationAdminSpecification
                                .withFilters(
                                        query.getSearch(),
                                        statuses,
                                        fromDate,
                                        toDate
                                ),
                        pageable
                );

        log.info(
                "Fetching applications page={} size={}",
                query.getPage(),
                query.getSize()
        );

        return applications.map(
                this::mapApp
        );
    }

    // =====================================
    // GET APP
    // =====================================

    @Override
    @Transactional(readOnly = true)
    public ApplicationDetailResponse getApp(Long id) {

        log.info(
                "Fetching app id={}",
                id
        );

        return mapApp(
                getAppOrThrow(id)
        );
    }

    // =====================================
    // SUSPEND APP
    // =====================================

    @Override
    public ApplicationDetailResponse suspendApp(Long id) {
        DeveloperApplication app = getAppOrThrow(id);

        if (app.getStatus() == ApplicationStatus.SUSPENDED_BY_ADMIN) {
            throw new InvalidRequestException("App already suspended");
        }

        if (app.getStatus() == ApplicationStatus.REVOKED) {
            throw new InvalidRequestException("Cannot suspend this app");
        }

        app.setStatus(ApplicationStatus.SUSPENDED_BY_ADMIN);
        DeveloperApplication updated = appRepository.save(app);

        DeveloperAccount developer = getApplicationOwner(updated);
        notificationService.sendApplicationSuspendedNotification(developer.getIdentifier(), updated.getApplicationName());
        auditService.logAdminEvent(updated.getClientId(), "/admin/apps/" + id + "/suspend", AuditEventType.APP_SUSPENDED, 200);

        log.info("Application suspended={}", updated.getClientId());
        return mapApp(updated);
    }

    // =====================================
    // ACTIVE APP
    // =====================================

    @Override
    public ApplicationDetailResponse activateApp(Long id) {
        DeveloperApplication app = getAppOrThrow(id);

        if (app.getStatus() != ApplicationStatus.SUSPENDED_BY_ADMIN) {
            throw new InvalidRequestException("Only admin suspended applications can be activated");
        }

        app.setStatus(ApplicationStatus.ACTIVE);
        DeveloperApplication updated = appRepository.save(app);

        DeveloperAccount developer = getApplicationOwner(updated);
        notificationService.sendApplicationReactivatedNotification(developer.getIdentifier(), updated.getApplicationName());
        auditService.logAdminEvent(updated.getClientId(), "/admin/apps/" + id + "/activate", AuditEventType.APP_ACTIVATED, 200);

        log.info("Application activated={}", updated.getClientId());
        return mapApp(updated);
    }

    // =====================================
    // REVOKE APP
    // =====================================

    @Override
    public ApplicationDetailResponse revokeApp(
            Long id
    ) {

        DeveloperApplication app =
                getAppOrThrow(id);

        if (app.getStatus()
                == ApplicationStatus.REVOKED) {

            throw new InvalidRequestException(
                    "App already revoked"
            );
        }

        app.setStatus(
                ApplicationStatus.REVOKED
        );

        DeveloperApplication updated =
                appRepository.save(app);

        DeveloperAccount developer =
                getApplicationOwner(updated);

        notificationService
                .sendApplicationRevokedNotification(
                        developer.getIdentifier(),
                        updated.getApplicationName()
                );

        auditService.logAdminEvent(
                updated.getClientId(),
                "/admin/apps/" + id + "/revoke",
                AuditEventType.APP_REVOKED_BY_ADMIN,
                200
        );

        log.info(
                "Application revoked={}",
                updated.getClientId()
        );

        return mapApp(updated);
    }



    // =====================================
    // GET WEBHOOK LOGS
    // =====================================

    @Override
    @Transactional(readOnly = true)
    public Page<WebhookLogResponse>
    getWebhookLogs(
            PageQuery query,
            List<WebhookStatus> statuses,
            List<WebhookEventType> eventTypes,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {

            throw new InvalidRequestException(
                    "fromDate cannot be after toDate"
            );
        }

        Pageable pageable =
                PageableFactory.create(
                        query
                );

        Page<WebhookLog> logs =
                webhookLogRepository.findAll(
                        AdminWebhookLogSpecification.withFilters(
                                query.getSearch(),
                                statuses,
                                eventTypes,
                                fromDate,
                                toDate
                        ),
                        pageable
                );

        log.info(
                "Fetching webhook logs page={} size={}",
                query.getPage(),
                query.getSize()
        );

        return logs.map(
                this::mapWebhook
        );
    }

    // =====================================
    // INTERNAL HELPERS
    // =====================================

    private DeveloperAccount getDeveloperOrThrow(
            Long id
    ) {

        return developerRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Developer not found"
                        )
                );
    }

    private DeveloperApplication getAppOrThrow(
            Long id
    ) {

        return appRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "App not found"
                        )
                );
    }

    private DeveloperAccount getApplicationOwner(
            DeveloperApplication app
    ) {

        return developerRepository.findById(
                        app.getDeveloper().getId()
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Developer not found"
                        )
                );
    }

    private DeveloperAccountResponse mapDeveloper(
            DeveloperAccount developer
    ) {

        return DeveloperAccountResponse.builder()
                .id(
                        developer.getId()
                )
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

    private ApplicationDetailResponse mapApp(
            DeveloperApplication app
    ) {

        return ApplicationDetailResponse.builder()
                .id(
                        app.getId()
                )
                .applicationName(
                        app.getApplicationName()
                )
                .clientId(
                        app.getClientId()
                )
                .status(
                        app.getStatus()
                )
                .createdAt(
                        app.getCreatedAt()
                )
                .build();
    }

    private WebhookLogResponse mapWebhook(
            WebhookLog log
    ) {

        return WebhookLogResponse.builder()
                .id(
                        log.getId()
                )
                .targetUrl(
                        log.getTargetUrl()
                )
                .status(
                        log.getStatus().name()
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
                .build();
    }
}