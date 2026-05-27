package com.shivamai.otp.account.service;

import com.shivamai.otp.account.dto.request.DeleteAccountRequest;
import com.shivamai.otp.account.dto.response.DeveloperDashboardMetricsResponse;
import com.shivamai.otp.account.dto.response.DeveloperProfileResponse;

import com.shivamai.otp.application.dto.request.CreateApplicationRequest;

import com.shivamai.otp.application.dto.response.ApplicationCreationResponse;
import com.shivamai.otp.application.dto.response.ApplicationSecretResponse;
import com.shivamai.otp.application.dto.response.DeveloperApplicationDetailsResponse;
import com.shivamai.otp.application.dto.response.DeveloperApplicationSummaryResponse;

import com.shivamai.otp.application.enums.ApplicationStatus;

import com.shivamai.otp.webhook.dto.ApplicationWebhookLogResponse;
import com.shivamai.otp.webhook.dto.WebhookSecretResponse;
import com.shivamai.otp.webhook.enums.WebhookEventType;
import com.shivamai.otp.webhook.enums.WebhookStatus;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;

import org.springframework.stereotype.Service;

@Service
public interface DeveloperAccountService {
    DeveloperProfileResponse getProfile();

    void deleteAccount(DeleteAccountRequest request, HttpServletRequest httpRequest);

    DeveloperDashboardMetricsResponse getMetrics();
}