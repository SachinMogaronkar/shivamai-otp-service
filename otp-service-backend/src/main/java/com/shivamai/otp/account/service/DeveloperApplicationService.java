package com.shivamai.otp.account.service;

import com.shivamai.otp.application.dto.request.CreateApplicationRequest;

import com.shivamai.otp.application.dto.response.ApplicationCreationResponse;
import com.shivamai.otp.application.dto.response.ApplicationSecretResponse;
import com.shivamai.otp.application.dto.response.DeveloperApplicationDetailsResponse;
import com.shivamai.otp.application.dto.response.DeveloperApplicationSummaryResponse;

import com.shivamai.otp.application.enums.ApplicationStatus;

import com.shivamai.otp.webhook.dto.WebhookSecretResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;

import org.springframework.stereotype.Service;

@Service
public interface DeveloperApplicationService {

    ApplicationCreationResponse createApp(CreateApplicationRequest request);

    Page<DeveloperApplicationSummaryResponse> getApps(
            int page,
            int size,
            String sortBy,
            String sort,
            String search,
            ApplicationStatus status
    );

    DeveloperApplicationDetailsResponse getAppDetails(Long appId);

    DeveloperApplicationSummaryResponse disableApp(Long appId);

    DeveloperApplicationSummaryResponse enableApp(Long appId);

    ApplicationSecretResponse rotateSecret(Long appId);

    WebhookSecretResponse rotateWebhookSecret(Long appId);

    void deleteApp(Long appId, HttpServletRequest httpRequest);
}