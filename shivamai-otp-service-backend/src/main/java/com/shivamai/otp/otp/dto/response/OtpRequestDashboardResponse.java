package com.shivamai.otp.otp.dto.response;

import com.shivamai.otp.otp.enums.OtpStatus;

import lombok.Builder;
import lombok.Getter;

import org.springframework.data.domain.Page;

import java.util.Map;

@Getter
@Builder
public class OtpRequestDashboardResponse {

    private Page<OtpRequestResponse> requests;

    private Map<OtpStatus, Long> statusCounts;
}