package com.shivamai.otp.usage.repository;

import com.shivamai.otp.usage.entity.OtpUsageSummary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

public interface OtpUsageSummaryRepository
        extends JpaRepository<OtpUsageSummary, Long>,
        JpaSpecificationExecutor<OtpUsageSummary> {

    Optional<OtpUsageSummary> findByClientIdAndUsageDate(
            String clientId,
            LocalDate usageDate
    );
}