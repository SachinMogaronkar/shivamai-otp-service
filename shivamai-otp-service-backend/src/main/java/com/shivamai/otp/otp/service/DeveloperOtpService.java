package com.shivamai.otp.otp.service;

import com.shivamai.otp.common.pagination.PageQuery;


import com.shivamai.otp.otp.channel.OtpDeliveryChannel;
import com.shivamai.otp.otp.dto.response.OtpRequestDashboardResponse;
import com.shivamai.otp.otp.dto.response.OtpRequestResponse;
import com.shivamai.otp.otp.enums.OtpPurpose;
import com.shivamai.otp.otp.enums.OtpStatus;
import com.shivamai.otp.otp.enums.OtpType;

import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface DeveloperOtpService {

    OtpRequestDashboardResponse
    getOtpRequests(
            PageQuery query,
            List<OtpStatus> statuses,
            List<OtpDeliveryChannel> channels,
            List<OtpType> otpTypes,
            List<OtpPurpose> purposes,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );
}