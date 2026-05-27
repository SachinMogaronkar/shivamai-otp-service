package com.shivamai.otp.otp.service;

import com.shivamai.otp.account.entity.DeveloperAccount;

import com.shivamai.otp.account.repository.DeveloperAccountRepository;

import com.shivamai.otp.common.exception.InvalidRequestException;
import com.shivamai.otp.common.exception.ResourceNotFoundException;
import com.shivamai.otp.common.exception.UnauthorizedException;

import com.shivamai.otp.common.pagination.PageQuery;
import com.shivamai.otp.common.pagination.PageableFactory;

import com.shivamai.otp.otp.channel.OtpDeliveryChannel;
import com.shivamai.otp.otp.dto.response.OtpRequestDashboardResponse;
import com.shivamai.otp.otp.dto.response.OtpRequestResponse;

import com.shivamai.otp.otp.entity.OtpRequest;
import com.shivamai.otp.otp.enums.OtpPurpose;
import com.shivamai.otp.otp.enums.OtpStatus;
import com.shivamai.otp.otp.enums.OtpType;

import com.shivamai.otp.otp.repository.OtpRequestRepository;

import com.shivamai.otp.otp.specification.OtpRequestSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeveloperOtpServiceImpl
        implements DeveloperOtpService {

    private final OtpRequestRepository repository;

    private final DeveloperAccountRepository developerRepository;

    @Override
    public OtpRequestDashboardResponse
    getOtpRequests(
            PageQuery query,
            List<OtpStatus> statuses,
            List<OtpDeliveryChannel> channels,
            List<OtpType> otpTypes,
            List<OtpPurpose> purposes,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        DeveloperAccount developer =
                getCurrentDeveloper();

        Pageable pageable =
                PageableFactory.create(
                        query
                );

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {

            throw new InvalidRequestException(
                    "From Date cannot be after To Date"
            );
        }

        Page<OtpRequest> requests =
                repository.findAll(
                        OtpRequestSpecification.withFilters(
                                developer.getIdentifier(),
                                query.getSearch(),
                                statuses,
                                channels,
                                otpTypes,
                                purposes,
                                fromDate,
                                toDate
                        ),
                        pageable
                );

        Page<OtpRequestResponse> responsePage =
                requests.map(
                        request ->
                                OtpRequestResponse.builder()
                                        .id(
                                                request.getId()
                                        )
                                        .identifier(
                                                request.getIdentifier()
                                        )
                                        .applicationName(
                                                request.getApplicationName()
                                        )
                                        .status(
                                                request.getStatus()
                                        )
                                        .channel(
                                                request.getChannel()
                                        )
                                        .otpType(
                                                request.getOtpType()
                                        )
                                        .purpose(
                                                request.getPurpose()
                                        )
                                        .attemptCount(
                                                request.getAttemptCount()
                                        )
                                        .createdAt(
                                                request.getCreatedAt()
                                        )
                                        .expiresAt(
                                                request.getExpiresAt()
                                        )
                                        .verifiedAt(
                                                request.getVerifiedAt()
                                        )
                                        .build()
                );

        Map<OtpStatus, Long> statusCounts =
                requests.getContent()
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        OtpRequest::getStatus,
                                        Collectors.counting()
                                )
                        );

        return OtpRequestDashboardResponse
                .builder()
                .requests(
                        responsePage
                )
                .statusCounts(
                        statusCounts
                )
                .build();
    }

    private DeveloperAccount getCurrentDeveloper() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new UnauthorizedException(
                    "Unauthorized"
            );
        }

        return developerRepository
                .findByIdentifier(
                        authentication.getName()
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Developer not found"
                                )
                );
    }
}