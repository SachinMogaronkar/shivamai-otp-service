package com.shivamai.otp.usage.service;

import com.shivamai.otp.common.pagination.PageQuery;
import com.shivamai.otp.common.pagination.PageableFactory;
import com.shivamai.otp.usage.dto.OtpUsageResponse;

import com.shivamai.otp.usage.entity.OtpUsageSummary;

import com.shivamai.otp.common.exception.InvalidRequestException;

import com.shivamai.otp.usage.repository.OtpUsageSummaryRepository;

import com.shivamai.otp.usage.specification.OtpUsageSummarySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpUsageService {

    private final OtpUsageSummaryRepository repository;

    private final ConcurrentHashMap<String, Object> locks =
            new ConcurrentHashMap<>();

    // =====================================
    // OTP REQUEST TRACKING
    // =====================================

    public void recordOtpRequest(
            String clientId
    ) {

        if (clientId == null
                || clientId.isBlank()) {

            return;
        }

        Object lock =
                locks.computeIfAbsent(
                        clientId,
                        key -> new Object()
                );

        synchronized (lock) {

            OtpUsageSummary usage =
                    getOrCreate(clientId);

            usage.setOtpRequests(
                    usage.getOtpRequests() + 1
            );

            repository.save(usage);

            log.debug(
                    "OTP request recorded for clientId={}",
                    clientId
            );
        }
    }

    // =====================================
    // OTP VERIFICATION TRACKING
    // =====================================

    public void recordOtpVerification(
            String clientId
    ) {

        if (clientId == null
                || clientId.isBlank()) {

            return;
        }

        Object lock =
                locks.computeIfAbsent(
                        clientId,
                        key -> new Object()
                );

        synchronized (lock) {

            OtpUsageSummary usage =
                    getOrCreate(clientId);

            usage.setOtpVerified(
                    usage.getOtpVerified() + 1
            );

            repository.save(usage);

            log.debug(
                    "OTP verification recorded for clientId={}",
                    clientId
            );
        }
    }

    // =====================================
    // TODAY USAGE
    // =====================================

    public long getTodayUsage(
            String clientId
    ) {

        if (clientId == null
                || clientId.isBlank()) {

            return 0;
        }

        return repository.findByClientIdAndUsageDate(
                        clientId,
                        LocalDate.now()
                )
                .map(OtpUsageSummary::getOtpRequests)
                .orElse(0);
    }

    // =====================================
    // ADMIN REPORTING
    // =====================================

    public Page<OtpUsageResponse> getPlatformUsage(
            PageQuery query,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        log.info("Fetching platform usage");

        Pageable pageable =
                PageableFactory.create(query);

        Page<OtpUsageSummary> usage =
                repository.findAll(
                        OtpUsageSummarySpecification.withFilters(
                                query.getSearch(),
                                fromDate,
                                toDate
                        ),
                        pageable
                );

        return usage.map(
                this::mapResponse
        );
    }

    public Page<OtpUsageResponse> getUsageForClient(
            String clientId,
            PageQuery query,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        if (clientId == null
                || clientId.isBlank()) {

            throw new InvalidRequestException(
                    "ClientId is required"
            );
        }

        log.info(
                "Fetching usage for clientId={}",
                clientId
        );

        Pageable pageable =
                PageableFactory.create(query);

        Page<OtpUsageSummary> usage =
                repository.findAll(
                        OtpUsageSummarySpecification
                                .withClientFilters(
                                        clientId,
                                        query.getSearch(),
                                        fromDate,
                                        toDate
                                ),
                        pageable
                );

        return usage.map(
                this::mapResponse
        );

    }

    // =====================================
    // INTERNAL HELPERS
    // =====================================

    private OtpUsageSummary getOrCreate(
            String clientId
    ) {

        LocalDate today =
                LocalDate.now();

        return repository.findByClientIdAndUsageDate(
                        clientId,
                        today
                )
                .orElseGet(() ->
                        OtpUsageSummary.builder()
                                .clientId(clientId)
                                .usageDate(today)
                                .otpRequests(0)
                                .otpVerified(0)
                                .createdAt(LocalDateTime.now())
                                .build()
                );
    }

    private OtpUsageResponse mapResponse(
            OtpUsageSummary usage
    ) {

        return OtpUsageResponse.builder()
                .clientId(
                        usage.getClientId()
                )
                .date(
                        usage.getUsageDate()
                )
                .otpRequests(
                        usage.getOtpRequests()
                )
                .otpVerified(
                        usage.getOtpVerified()
                )
                .build();
    }
}