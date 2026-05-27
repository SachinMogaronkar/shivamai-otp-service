package com.shivamai.otp.account.service;

import com.shivamai.otp.account.dto.request.DeleteAccountRequest;
import com.shivamai.otp.account.dto.response.DeveloperDashboardMetricsResponse;
import com.shivamai.otp.account.dto.response.DeveloperProfileResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

@Service
public interface DeveloperAccountService {
    DeveloperProfileResponse getProfile();

    void deleteAccount(DeleteAccountRequest request, HttpServletRequest httpRequest);

    DeveloperDashboardMetricsResponse getMetrics();
}