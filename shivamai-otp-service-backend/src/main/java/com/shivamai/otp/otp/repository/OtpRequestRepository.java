package com.shivamai.otp.otp.repository;

import com.shivamai.otp.account.enums.AccountRole;

import com.shivamai.otp.otp.entity.OtpRequest;

import com.shivamai.otp.otp.enums.OtpStatus;
import com.shivamai.otp.otp.enums.OtpType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

@Repository
public interface OtpRequestRepository
        extends JpaRepository<OtpRequest, Long>,
        JpaSpecificationExecutor<OtpRequest> {

    Optional<OtpRequest>
    findTopByIdentifierAndApplicationNameAndPurposeAndStatusInAndExpiresAtAfterOrderByCreatedAtDesc(
            String identifier,
            String applicationName,
            com.shivamai.otp.otp.enums.OtpPurpose purpose,
            java.util.List<com.shivamai.otp.otp.enums.OtpStatus> statuses,
            java.time.LocalDateTime currentTime
    );

    long countByIdentifierAndCreatedAtAfter(
            String identifier,
            LocalDateTime createdAt
    );

    long countByApplicationNameAndCreatedAtAfter(
            String applicationName,
            LocalDateTime createdAt
    );

    long countByStatus(
            OtpStatus status
    );

    long countByStatusIn(
            List<OtpStatus> statuses
    );
}