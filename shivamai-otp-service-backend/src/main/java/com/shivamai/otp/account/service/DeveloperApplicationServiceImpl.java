package com.shivamai.otp.account.service;

import com.shivamai.otp.application.dto.request.CreateApplicationRequest;

import com.shivamai.otp.application.dto.response.ApplicationCreationResponse;
import com.shivamai.otp.application.dto.response.ApplicationSecretResponse;
import com.shivamai.otp.application.dto.response.DeveloperApplicationDetailsResponse;
import com.shivamai.otp.application.dto.response.DeveloperApplicationSummaryResponse;

import com.shivamai.otp.account.entity.DeveloperAccount;
import com.shivamai.otp.application.entity.DeveloperApplication;

import com.shivamai.otp.account.enums.DeveloperAccountStatus;
import com.shivamai.otp.application.enums.ApplicationStatus;

import com.shivamai.otp.application.specification.DeveloperApplicationSpecification;
import com.shivamai.otp.audit.enums.AuditEventType;

import com.shivamai.otp.common.exception.*;

import com.shivamai.otp.common.pagination.PageQuery;
import com.shivamai.otp.common.pagination.PageableFactory;
import com.shivamai.otp.common.security.ApplicationSessionInvalidationService;
import com.shivamai.otp.common.util.ApplicationCredentialGenerator;

import com.shivamai.otp.account.repository.DeveloperAccountRepository;
import com.shivamai.otp.application.repository.DeveloperApplicationRepository;

import com.shivamai.otp.audit.service.AuditService;
import com.shivamai.otp.notification.service.NotificationService;

import com.shivamai.otp.webhook.dto.WebhookSecretResponse;
import jakarta.servlet.http.HttpServletRequest;

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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeveloperApplicationServiceImpl
        implements DeveloperApplicationService {

    private final DeveloperAccountRepository developerRepository;

    private final DeveloperApplicationRepository appRepository;

    private final PasswordEncoder passwordEncoder;

    private final NotificationService notificationService;

    private final AuditService auditService;

    private final ApplicationSessionInvalidationService sessionInvalidationService;

    // =====================================
    // CREATE APP
    // =====================================

    @Override
    public ApplicationCreationResponse createApp(
            CreateApplicationRequest request
    ) {

        DeveloperAccount developer =
                getCurrentDeveloper();

        validateDeveloperAccess(
                developer
        );

        String webhookUrl =
                request.getWebhookUrl()
                        .trim()
                        .toLowerCase();

        log.info(
                "Creating app for developer={}",
                developer.getIdentifier()
        );

        String clientId;

        do {

            clientId =
                    ApplicationCredentialGenerator.generateClientId();

        } while (
                appRepository.existsByClientId(clientId)
        );



        String applicationName =
                request.getApplicationName()
                        .trim();

        if (appRepository.existsByDeveloperIdAndApplicationName(
                developer.getId(),
                applicationName
        )) {

            throw new ResourceConflictException(
                    "Application name already exists"
            );
        }

        String rawSecret = ApplicationCredentialGenerator.generateClientSecret();

        DeveloperApplication app =
                DeveloperApplication.builder()
                        .developer(
                                developer
                        )
                        .applicationName(
                                request.getApplicationName()
                        )
                        .clientId(
                                clientId
                        )
                        .clientSecretHash(
                                passwordEncoder.encode(rawSecret)
                        )
                        .webhookUrl(
                                webhookUrl
                        )
                        .status(
                                ApplicationStatus.ACTIVE
                        )
                        .build();

        DeveloperApplication savedApp =
                appRepository.save(app);

        auditService.logDeveloperEvent(
                savedApp.getClientId(),
                developer.getIdentifier(),
                "/developer/dashboard/apps",
                AuditEventType.APP_CREATED,
                201
        );

        try {

            notificationService
                    .sendApplicationCreatedNotification(
                            developer.getIdentifier(),
                            savedApp.getApplicationName()
                    );

            notificationService
                    .sendApplicationSecretGeneratedNotification(
                            developer.getIdentifier(),
                            savedApp.getApplicationName()
                    );

        } catch (Exception e) {

            log.error(
                    "Application notification failed clientId={}",
                    savedApp.getClientId(),
                    e
            );
        }

        log.info(
                "Application created successfully appId={} clientId={}",
                savedApp.getId(),
                savedApp.getClientId()
        );

        return ApplicationCreationResponse.builder()
                .credentials(
                        ApplicationSecretResponse.builder()
                                .clientId(
                                        savedApp.getClientId()
                                )
                                .clientSecret(
                                        rawSecret
                                )
                                .build()
                )
                .message(
                        "Store this client secret securely. It cannot be viewed again."
                )
                .build();
    }

    // =====================================
    // GET APPS
    // =====================================

    @Override
    @Transactional(readOnly = true)
    public Page<DeveloperApplicationSummaryResponse> getApps(
            int page,
            int size,
            String sortBy,
            String sort,
            String search,
            ApplicationStatus status
    ) {

        DeveloperAccount developer =
                getCurrentDeveloper();

        validateDeveloperAccess(
                developer
        );

        PageQuery query =
                PageQuery.builder()
                        .page(page)
                        .size(size)
                        .sortBy(sortBy)
                        .direction(sort)
                        .search(search)
                        .build();

        Pageable pageable =
                PageableFactory.create(
                        query
                );

        Page<DeveloperApplication> applications =
                appRepository.findAll(
                        DeveloperApplicationSpecification
                                .withFilters(
                                        developer.getId(),
                                        search,
                                        status
                                ),
                        pageable
                );

        return applications.map(
                app ->
                        mapApplicationSummary(
                                app,
                                "Application fetched successfully"
                        )
        );
    }
    // =====================================
    // APP DETAILS
    // =====================================

    @Override
    public DeveloperApplicationDetailsResponse getAppDetails(
            Long appId
    ) {

        DeveloperAccount developer =
                getCurrentDeveloper();

        DeveloperApplication app =
                getOwnedApp(
                        appId,
                        developer
                );

        return DeveloperApplicationDetailsResponse
                .builder()
                .id(
                        app.getId()
                )
                .applicationName(
                        app.getApplicationName()
                )
                .clientId(
                        app.getClientId()
                )
                .webhookUrl(
                        app.getWebhookUrl()
                )
                .status(
                        app.getStatus()
                )
                .createdAt(
                        app.getCreatedAt()
                )
                .updatedAt(
                        app.getUpdatedAt()
                )
                .secretRotatedAt(
                        app.getSecretRotatedAt()
                )
                .build();
    }

    // =====================================
    // DISABLE APP
    // =====================================

    @Override
    public DeveloperApplicationSummaryResponse disableApp(
            Long appId
    ) {

        DeveloperAccount developer =
                getCurrentDeveloper();

        validateDeveloperAccess(
                developer
        );

        DeveloperApplication app =
                getOwnedApp(
                        appId,
                        developer
                );

        if (app.getStatus()
                == ApplicationStatus.REVOKED) {

            throw new ForbiddenOperationException(
                    "This application has been revoked by admin and cannot be accessed"
            );
        }

        if (app.getStatus()
                == ApplicationStatus.SUSPENDED_BY_ADMIN) {

            throw new ForbiddenOperationException(
                    "This application has been suspended by admin and cannot be accessed"
            );
        }

        if (app.getStatus()
                == ApplicationStatus.DISABLED_BY_OWNER) {

            throw new ForbiddenOperationException(
                    "Application already disabled"
            );
        }

        if (app.getStatus()
                != ApplicationStatus.ACTIVE) {

            throw new ForbiddenOperationException(
                    "Only active applications can be disabled"
            );
        }

        app.setStatus(
                ApplicationStatus.DISABLED_BY_OWNER
        );

        DeveloperApplication updated =
                appRepository.save(app);

        auditService.logDeveloperEvent(
                updated.getClientId(),
                developer.getIdentifier(),
                "/developer/dashboard/apps/" + appId + "/disable",
                AuditEventType.APP_DISABLED_BY_DEVELOPER,
                200
        );

        notificationService
                .sendApplicationDisabledNotification(
                        developer.getIdentifier(),
                        updated.getApplicationName()
                );

        log.info(
                "Application disabled by developer={}",
                updated.getClientId()
        );

        return mapApplicationSummary(
                updated,
                "Application disabled successfully"
        );
    }

    // =====================================
    // ENABLE APP
    // =====================================

    @Override
    public DeveloperApplicationSummaryResponse enableApp(
            Long appId
    ) {

        DeveloperAccount developer =
                getCurrentDeveloper();

        validateDeveloperAccess(
                developer
        );

        DeveloperApplication app =
                getOwnedApp(
                        appId,
                        developer
                );

        if (app.getStatus()
                == ApplicationStatus.REVOKED) {

            throw new ForbiddenOperationException(
                    "This application has been revoked by admin and cannot be accessed"
            );
        }

        if (app.getStatus()
                == ApplicationStatus.SUSPENDED_BY_ADMIN) {

            throw new ForbiddenOperationException(
                    "This application has been suspended by admin and cannot be accessed"
            );
        }

        if (app.getStatus()
                != ApplicationStatus.DISABLED_BY_OWNER) {

            throw new ForbiddenOperationException(
                    "Only owner-disabled applications can be enabled"
            );
        }

        app.setStatus(
                ApplicationStatus.ACTIVE
        );

        DeveloperApplication updated =
                appRepository.save(app);

        auditService.logDeveloperEvent(
                updated.getClientId(),
                developer.getIdentifier(),
                "/developer/dashboard/apps/" + appId + "/enable",
                AuditEventType.APP_ENABLED_BY_DEVELOPER,
                200
        );

        notificationService
                .sendApplicationEnabledNotification(
                        developer.getIdentifier(),
                        updated.getApplicationName()
                );

        log.info(
                "Application enabled by developer={}",
                updated.getClientId()
        );

        return mapApplicationSummary(
                updated,
                "Application enabled successfully"
        );
    }

    // =====================================
    // ROTATE SECRET
    // =====================================

    @Override
    public ApplicationSecretResponse rotateSecret(
            Long appId
    ) {

        DeveloperAccount developer = getCurrentDeveloper();

        validateDeveloperAccess(developer);

        DeveloperApplication app = getOwnedApp(appId, developer);

        if (app.getStatus() == ApplicationStatus.REVOKED) {

            throw new InvalidRequestException(
                    "This application has been revoked by admin and cannot be accessed"
            );
        }

        if (app.getStatus() == ApplicationStatus.SUSPENDED_BY_ADMIN) {

            throw new ForbiddenOperationException(
                    "This application has been suspended by admin and cannot be accessed"
            );
        }

        if (app.getStatus() != ApplicationStatus.ACTIVE) {

            throw new ForbiddenOperationException(
                    "Cannot rotate secret for inactive application"
            );
        }

        if (app.getSecretRotatedAt() != null && app.getSecretRotatedAt().isAfter(LocalDateTime.now().minusMinutes(5))) {
            throw new RateLimitExceededException(
                    "Secret can only be rotated once every 5 minutes"
            );
        }

        String newSecret = ApplicationCredentialGenerator.generateClientSecret();

        app.setClientSecretHash(passwordEncoder.encode(newSecret));

        app.setSecretRotatedAt(LocalDateTime.now());

        sessionInvalidationService
                .invalidateApplicationSessions(
                        app.getClientId()
                );

        try {

            notificationService
                    .sendApplicationSecretRotatedNotification(
                            developer.getIdentifier(),
                            app.getApplicationName()
                    );

        } catch (Exception e) {

            log.error(
                    "Secret rotation notification failed clientId={}",
                    app.getClientId(),
                    e
            );
        }

        log.info(
                "Application secret rotated clientId={}",
                app.getClientId()
        );

        auditService.logDeveloperEvent(
                app.getClientId(),
                developer.getIdentifier(),
                "/developer/dashboard/apps/" + appId + "/rotate-secret",
                AuditEventType.APPLICATION_SECRET_ROTATED,
                200
        );

        return ApplicationSecretResponse.builder()
                .clientId(app.getClientId())
                .clientSecret(newSecret)
                .build();
    }

    @Override
    public WebhookSecretResponse rotateWebhookSecret(
            Long appId
    ) {

        DeveloperAccount developer =
                getCurrentDeveloper();

        DeveloperApplication app =
                getOwnedApp(
                        appId,
                        developer
                );

        String newSecret =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "");

        app.setWebhookSecret(
                newSecret
        );

        appRepository.save(
                app
        );

        log.info(
                "Webhook secret rotated appId={}",
                appId
        );

        return WebhookSecretResponse.builder()
                .webhookSecret(
                        newSecret
                )
                .message(
                        "Webhook secret rotated successfully"
                )
                .build();
    }

    // =====================================
    // DELETE APP
    // =====================================

    @Override
    public void deleteApp(Long appId, HttpServletRequest httpRequest) {

        DeveloperAccount developer = getCurrentDeveloper();

        validateDeveloperAccess(developer);

        DeveloperApplication app = getOwnedApp(appId, developer);

        if (app.getStatus() == ApplicationStatus.REVOKED) {

            throw new ForbiddenOperationException(
                    "Revoked application cannot be deleted"
            );
        }

        if (app.getStatus() == ApplicationStatus.SUSPENDED_BY_ADMIN) {

            throw new ForbiddenOperationException(
                    "Suspended application cannot be deleted"
            );
        }

        appRepository.delete(app);

        auditService.logDeveloperEvent(
                app.getClientId(),
                developer.getIdentifier(),
                "/developer/dashboard/apps/" + appId + "/delete",
                AuditEventType.APP_DELETED_BY_DEVELOPER,
                200
        );

        log.info(
                "Application deleted by developer={}",
                app.getClientId()
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

    // =====================================
    // MAP APP SUMMARY
    // =====================================

    private DeveloperApplicationSummaryResponse mapApplicationSummary(DeveloperApplication app, String message) {

        return DeveloperApplicationSummaryResponse.builder()
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
                .message(
                        message
                )
                .build();
    }
}