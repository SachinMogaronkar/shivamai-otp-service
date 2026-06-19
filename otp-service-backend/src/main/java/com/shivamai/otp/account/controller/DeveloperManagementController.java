package com.shivamai.otp.account.controller;

import com.shivamai.otp.account.enums.DeveloperAccountStatus;
import com.shivamai.otp.application.enums.ApplicationStatus;
import com.shivamai.otp.common.pagination.PageQuery;
import com.shivamai.otp.common.response.ApiResponse;

import com.shivamai.otp.application.dto.response.ApplicationDetailResponse;

import com.shivamai.otp.account.dto.response.DeveloperAccountResponse;

import com.shivamai.otp.webhook.dto.WebhookLogResponse;

import com.shivamai.otp.account.service.DeveloperManagementService;

import com.shivamai.otp.webhook.enums.WebhookEventType;
import com.shivamai.otp.webhook.enums.WebhookStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import java.util.List;

@RestController
@RequestMapping("/admin")

@RequiredArgsConstructor
@Slf4j
public class DeveloperManagementController {

    private final DeveloperManagementService
            developerManagementService;

    // =====================================
    // DEVELOPERS
    // =====================================

    @GetMapping("/developers/pending")
    public ApiResponse<Page<DeveloperAccountResponse>>
    getPendingDevelopers(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            LocalDateTime toDate
    ) {

        log.info(
                "Fetching pending developers"
        );

        PageQuery query =
                PageQuery.builder()
                        .page(page)
                        .size(size)
                        .sortBy(sortBy)
                        .direction(direction)
                        .search(search)
                        .build();

        return success(
                "Pending developers",
                developerManagementService
                        .getPendingDevelopers(
                                query,
                                fromDate,
                                toDate
                        )
        );
    }

    @GetMapping("/developers")
    public ApiResponse<Page<DeveloperAccountResponse>>
    getDevelopers(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            List<DeveloperAccountStatus> statuses,

            @RequestParam(required = false)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            LocalDateTime toDate
    ) {

        log.info(
                "Fetching developers"
        );

        PageQuery query =
                PageQuery.builder()
                        .page(page)
                        .size(size)
                        .sortBy(sortBy)
                        .direction(direction)
                        .search(search)
                        .build();

        return success(
                "Developers retrieved",
                developerManagementService
                        .getDevelopers(
                                query,
                                statuses,
                                fromDate,
                                toDate
                        )
        );
    }

    @GetMapping("/developers/{id}")
    public ApiResponse<DeveloperAccountResponse>
    getDeveloper(
            @PathVariable
            Long id
    ) {

        log.info(
                "Fetching developer id={}",
                id
        );

        return success(
                "Developer details",
                developerManagementService
                        .getDeveloper(id)
        );
    }

    @PatchMapping("/developers/{id}/approve")
    public ApiResponse<DeveloperAccountResponse>
    approveDeveloper(
            @PathVariable
            Long id
    ) {

        log.info(
                "Approving developer id={}",
                id
        );

        return success(
                "Developer approved",
                developerManagementService
                        .approveDeveloper(id)
        );
    }

    @PatchMapping("/developers/{id}/suspend")
    public ApiResponse<DeveloperAccountResponse>
    suspendDeveloper(
            @PathVariable
            Long id
    ) {

        log.info(
                "Suspending developer id={}",
                id
        );

        return success(
                "Developer suspended",
                developerManagementService
                        .suspendDeveloper(id)
        );
    }

    @PatchMapping("/developers/{id}/activate")
    public ApiResponse<DeveloperAccountResponse>
    activateDeveloper(
            @PathVariable
            Long id
    ) {

        log.info(
                "Activating developer id={}",
                id
        );

        return success(
                "Developer activated",
                developerManagementService
                        .activateDeveloper(id)
        );
    }

    @PatchMapping("/developers/{id}/revoke")
    public ApiResponse<DeveloperAccountResponse> revokeDeveloper(@PathVariable Long id) {
        log.info("Revoking developer id={}", id);
        DeveloperAccountResponse developerDto = developerManagementService.revokeDeveloper(id);
        return success("Developer revoked", developerDto);
    }

    // =====================================
    // APPLICATIONS
    // =====================================

    @GetMapping("/apps")
    public ApiResponse<Page<ApplicationDetailResponse>>
    getAllApps(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            List<ApplicationStatus> statuses,

            @RequestParam(required = false)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            LocalDateTime toDate
    ) {

        log.info(
                "Fetching all applications"
        );

        PageQuery query =
                PageQuery.builder()
                        .page(page)
                        .size(size)
                        .sortBy(sortBy)
                        .direction(direction)
                        .search(search)
                        .build();

        return success(
                "All apps retrieved",
                developerManagementService
                        .getAllApps(
                                query,
                                statuses,
                                fromDate,
                                toDate
                        )
        );
    }

    @GetMapping("/apps/{id}")
    public ApiResponse<ApplicationDetailResponse>
    getApp(
            @PathVariable
            Long id
    ) {

        log.info(
                "Fetching app id={}",
                id
        );

        return success(
                "App details",
                developerManagementService
                        .getApp(id)
        );
    }

    // =====================================
    // SUSPEND APP
    // =====================================

    @PatchMapping("/apps/{id}/suspend")
    public ApiResponse<ApplicationDetailResponse> suspendApp(@PathVariable Long id) {
        log.info("Suspending app id={}", id);
        ApplicationDetailResponse appDto = developerManagementService.suspendApp(id);
        return success("App suspended", appDto);
    }

    // =====================================
    // ACTIVATE APP
    // =====================================

    @PatchMapping("/apps/{id}/activate")
    public ApiResponse<ApplicationDetailResponse> activateApp(@PathVariable Long id) {
        log.info("Activating app id={}", id);
        ApplicationDetailResponse appDto = developerManagementService.activateApp(id);
        return success("App activated", appDto);
    }

    // =====================================
    // REVOKE APP
    // =====================================
    @PatchMapping("/apps/{id}/revoke")
    public ApiResponse<ApplicationDetailResponse> revokeApp(@PathVariable Long id) {
        log.info("Revoking app id={}", id);
        ApplicationDetailResponse appDto = developerManagementService.revokeApp(id);
        return success("App revoked", appDto);
    }

    // =====================================
    // WEBHOOKS
    // =====================================

    @GetMapping("/webhooks")
    public ApiResponse<Page<WebhookLogResponse>>
    getWebhooks(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction,

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
                "Fetching webhook logs"
        );

        PageQuery query =
                PageQuery.builder()
                        .page(page)
                        .size(size)
                        .sortBy(sortBy)
                        .direction(direction)
                        .search(search)
                        .build();

        return success(
                "Webhook logs",
                developerManagementService
                        .getWebhookLogs(
                                query,
                                statuses,
                                eventTypes,
                                fromDate,
                                toDate
                        )
        );
    }

    // =====================================
    // COMMON SUCCESS RESPONSE
    // =====================================

    private <T> ApiResponse<T>
    success(
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