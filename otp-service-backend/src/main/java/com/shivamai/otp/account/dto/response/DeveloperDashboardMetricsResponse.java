package com.shivamai.otp.account.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeveloperDashboardMetricsResponse {

    private long totalOtpRequests;

    private long successfulVerifications;

    private long failedVerifications;

    private long activeApplications;

    private long todayUsage;

    private double successRate;
}