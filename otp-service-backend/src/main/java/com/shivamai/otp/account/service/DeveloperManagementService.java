package com.shivamai.otp.account.service;

import com.shivamai.otp.account.dto.response.DeveloperAccountResponse;
import com.shivamai.otp.account.enums.DeveloperAccountStatus;
import com.shivamai.otp.application.dto.response.ApplicationDetailResponse;
import com.shivamai.otp.application.enums.ApplicationStatus;
import com.shivamai.otp.common.pagination.PageQuery;
import com.shivamai.otp.webhook.dto.WebhookLogResponse;
import com.shivamai.otp.webhook.enums.WebhookEventType;
import com.shivamai.otp.webhook.enums.WebhookStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface DeveloperManagementService {

    // =========================
    // DEVELOPER OPERATIONS
    // =========================
    Page<DeveloperAccountResponse>
    getPendingDevelopers(
            PageQuery query,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );

    Page<DeveloperAccountResponse> getDevelopers(PageQuery query,
            List<DeveloperAccountStatus> statuses,
            LocalDateTime fromDate,
            LocalDateTime toDate);

    DeveloperAccountResponse getDeveloper(Long id);

    DeveloperAccountResponse approveDeveloper(Long id);

    DeveloperAccountResponse suspendDeveloper(Long id);

    DeveloperAccountResponse activateDeveloper(Long id);

    DeveloperAccountResponse revokeDeveloper(Long id);

    // =========================
    // APPLICATION OPERATIONS
    // =========================
    Page<ApplicationDetailResponse>
    getAllApps(PageQuery query,
            List<ApplicationStatus> statuses,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );

    ApplicationDetailResponse getApp(Long id);

    ApplicationDetailResponse suspendApp(Long id);

    ApplicationDetailResponse activateApp(Long id);

    ApplicationDetailResponse revokeApp(Long id);


    // =========================
    // WEBHOOK LOGS
    // =========================
    Page<WebhookLogResponse>
    getWebhookLogs(PageQuery query,
            List<WebhookStatus> statuses,
            List<WebhookEventType> eventTypes,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );
}