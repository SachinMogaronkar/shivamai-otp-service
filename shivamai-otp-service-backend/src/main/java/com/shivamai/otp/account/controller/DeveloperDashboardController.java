package com.shivamai.otp.account.controller;

import com.shivamai.otp.account.dto.request.DeleteAccountRequest;
import com.shivamai.otp.account.dto.response.DeveloperDashboardMetricsResponse;
import com.shivamai.otp.account.dto.response.DeveloperProfileResponse;

import com.shivamai.otp.account.service.DeveloperAccountService;
import com.shivamai.otp.account.service.DeveloperApplicationService;

import com.shivamai.otp.account.service.DeveloperWebhookService;
import com.shivamai.otp.application.dto.request.CreateApplicationRequest;

import com.shivamai.otp.application.dto.response.ApplicationCreationResponse;
import com.shivamai.otp.application.dto.response.ApplicationSecretResponse;
import com.shivamai.otp.application.dto.response.DeveloperApplicationDetailsResponse;
import com.shivamai.otp.application.dto.response.DeveloperApplicationSummaryResponse;

import com.shivamai.otp.application.enums.ApplicationStatus;
import com.shivamai.otp.common.response.ApiResponse;

import com.shivamai.otp.webhook.dto.ApplicationWebhookLogResponse;
import com.shivamai.otp.webhook.dto.WebhookSecretResponse;
import com.shivamai.otp.webhook.enums.WebhookEventType;
import com.shivamai.otp.webhook.enums.WebhookStatus;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/developer/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DeveloperDashboardController {

    private final DeveloperAccountService accountService;
    private final DeveloperApplicationService applicationService;
    private final DeveloperWebhookService webhookService;

    // =====================================
    // PROFILE
    // =====================================

    @GetMapping("/profile")
    public ApiResponse<DeveloperProfileResponse> getProfile() {

        log.info(
                "Fetching developer profile"
        );

        return success(
                "Developer profile fetched successfully",
                accountService.getProfile()
        );
    }

    // =====================================
    // CREATE APPLICATION
    // =====================================

    @PostMapping("/apps")
    public ApiResponse<ApplicationCreationResponse> createApp(
            @Valid
            @RequestBody
            CreateApplicationRequest request
    ) {

        log.info(
                "Creating developer application"
        );

        return success(
                "Application created successfully",
                applicationService.createApp(
                        request
                )
        );
    }

    // =====================================
    // GET APPLICATIONS
    // =====================================

    @GetMapping("/apps")
    public ApiResponse<Page<DeveloperApplicationSummaryResponse>> getApps(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sort,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            ApplicationStatus status
    ) {

        log.info(
                "Fetching developer applications"
        );

        return success(
                "Developer applications fetched successfully",
                applicationService.getApps(
                        page,
                        size,
                        sortBy,
                        sort,
                        search,
                        status
                )
        );
    }

    // =====================================
    // GET APPLICATION DETAILS
    // =====================================

    @GetMapping("/apps/{id}")
    public ApiResponse<DeveloperApplicationDetailsResponse>
    getAppDetails(
            @PathVariable("id")
            Long appId
    ) {

        log.info(
                "Fetching developer application details id={}",
                appId
        );

        return success(
                "Developer application details fetched successfully",
                applicationService.getAppDetails(
                        appId
                )
        );
    }

    // =====================================
    // DISABLE APPLICATION
    // =====================================

    @PatchMapping("/apps/{id}/disable")
    public ApiResponse<DeveloperApplicationSummaryResponse> disableApp(
            @PathVariable("id")
            Long appId
    ) {

        log.info(
                "Disabling developer application id={}",
                appId
        );

        DeveloperApplicationSummaryResponse response =
                applicationService.disableApp(
                        appId
                );

        return success(
                response.getMessage(),
                response
        );
    }

    // =====================================
    // ENABLE APPLICATION
    // =====================================

    @PatchMapping("/apps/{id}/enable")
    public ApiResponse<DeveloperApplicationSummaryResponse> enableApp(
            @PathVariable("id")
            Long appId
    ) {

        log.info(
                "Enabling developer application id={}",
                appId
        );

        DeveloperApplicationSummaryResponse response =
                applicationService.enableApp(
                        appId
                );

        return success(
                response.getMessage(),
                response
        );
    }

    // =====================================
    // ROTATE SECRET
    // =====================================

    @PatchMapping("/apps/{id}/rotate-secret")
    public ApiResponse<ApplicationSecretResponse> rotateSecret(
            @PathVariable("id")
            Long appId
    ) {

        log.info(
                "Rotating application secret appId={}",
                appId
        );

        return success(
                "Application secret rotated successfully",
                applicationService.rotateSecret(
                        appId
                )
        );
    }

    // =====================================
    // ROTATE WEBHOOK SECRET
    // =====================================

    @PatchMapping("/apps/{id}/rotate-webhook-secret")
    public ApiResponse<WebhookSecretResponse> rotateWebhookSecret(@PathVariable("id") Long appId) {

        log.info(
                "Rotating webhook secret appId={}",
                appId
        );

        return success(
                "Webhook secret rotated successfully",
                applicationService.rotateWebhookSecret(
                        appId
                )
        );
    }

    // =====================================
    // GET WEBHOOK LOGS
    // =====================================

    @GetMapping("/apps/{id}/webhooks")
    public ApiResponse<Page<ApplicationWebhookLogResponse>> getWebhookLogs(
            @PathVariable("id")
            Long appId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sort,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            List<WebhookStatus> statuses,

            @RequestParam(required = false)
            List<WebhookEventType> eventTypes,

            @RequestParam(required = false)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            LocalDateTime toDate
    ) {

        log.info(
                "Fetching webhook logs appId={}",
                appId
        );

        return success(
                "Webhook logs fetched successfully",
                webhookService.getWebhookLogs(
                        appId,
                        page,
                        size,
                        sort,
                        search,
                        statuses,
                        eventTypes,
                        fromDate,
                        toDate
                )
        );
    }

    // =====================================
    // DASHBOARD METRICS
    // =====================================

    @GetMapping("/metrics")
    public ApiResponse<DeveloperDashboardMetricsResponse> getMetrics() {

        log.info(
                "Fetching developer dashboard metrics"
        );

        return success(
                "Developer dashboard metrics fetched successfully",
                accountService.getMetrics()
        );
    }



    // =====================================
    // DELETE APPLICATION
    // =====================================

    @DeleteMapping("/apps/{appId}")
    public ApiResponse<Void> deleteApp(
            @PathVariable
            Long appId,
            HttpServletRequest request
    ) {

        log.info(
                "Deleting developer application id={}",
                appId
        );

        applicationService.deleteApp(
                appId,
                request
        );

        return success(
                "Application deleted successfully",
                null
        );
    }

    // =====================================
    // DELETE ACCOUNT
    // =====================================

    @DeleteMapping("/account")
    public ApiResponse<Void> deleteAccount(
            @Valid
            @RequestBody
            DeleteAccountRequest request,
            HttpServletRequest httpRequest
    ) {

        log.info(
                "Deleting developer account"
        );

        accountService.deleteAccount(
                request,
                httpRequest
        );

        return success(
                "Developer account deleted successfully",
                null
        );
    }

    // =====================================
    // COMMON SUCCESS RESPONSE
    // =====================================

    private <T> ApiResponse<T> success(
            String message,
            T data
    ) {

        return new ApiResponse<>(
                "SUCCESS",
                message,
                data,
                LocalDateTime.now()
        );
    }
}