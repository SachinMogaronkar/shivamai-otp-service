package com.shivamai.otp.otp.controller;

import com.shivamai.otp.common.pagination.PageQuery;

import com.shivamai.otp.common.response.ApiResponse;

import com.shivamai.otp.otp.channel.OtpDeliveryChannel;
import com.shivamai.otp.otp.dto.response.OtpRequestDashboardResponse;
import com.shivamai.otp.otp.dto.response.OtpRequestResponse;

import com.shivamai.otp.otp.enums.OtpPurpose;
import com.shivamai.otp.otp.enums.OtpStatus;
import com.shivamai.otp.otp.enums.OtpType;

import com.shivamai.otp.otp.service.DeveloperOtpService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/developer/dashboard")
@RequiredArgsConstructor
public class DeveloperOtpController {

    private final DeveloperOtpService service;

    @GetMapping("/otp-requests")
    public ApiResponse<OtpRequestDashboardResponse> getOtpRequests(

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
            List<OtpStatus> statuses,

            @RequestParam(required = false)
            List<OtpDeliveryChannel> channels,

            @RequestParam(required = false)
            List<OtpType> otpTypes,

            @RequestParam(required = false)
            List<OtpPurpose> purposes,

            @RequestParam(required = false)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            LocalDateTime toDate
    ) {

        PageQuery query =
                PageQuery.builder()
                        .page(page)
                        .size(size)
                        .sortBy(sortBy)
                        .direction(direction)
                        .search(search)
                        .build();

        return new ApiResponse<>(
                "SUCCESS",
                "OTP requests fetched successfully",
                service.getOtpRequests(
                        query,
                        statuses,
                        channels,
                        otpTypes,
                        purposes,
                        fromDate,
                        toDate
                ),
                LocalDateTime.now()
        );
    }
}