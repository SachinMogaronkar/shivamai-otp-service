package com.shivamai.otp.usage.controller;

import com.shivamai.otp.common.pagination.PageQuery;
import com.shivamai.otp.common.response.ApiResponse;
import com.shivamai.otp.usage.dto.OtpUsageResponse;

import com.shivamai.otp.usage.service.OtpUsageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/usage")
@Slf4j
public class UsageAnalyticsController {

    private final OtpUsageService otpUsageService;

    @GetMapping
    public ApiResponse<Page<OtpUsageResponse>>
    getUsage(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "usageDate")
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
                "Fetching all usage data"
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
                "Platform usage",
                otpUsageService.getPlatformUsage(
                        query,
                        fromDate,
                        toDate
                )
        );
    }

    @GetMapping("/{clientId}")
    public ApiResponse<Page<OtpUsageResponse>>
    getUsageForClient(

            @PathVariable
            String clientId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "usageDate")
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
                "Fetching usage for clientId={}",
                clientId
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
                "Usage for client",
                otpUsageService.getUsageForClient(
                        clientId,
                        query,
                        fromDate,
                        toDate
                )
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