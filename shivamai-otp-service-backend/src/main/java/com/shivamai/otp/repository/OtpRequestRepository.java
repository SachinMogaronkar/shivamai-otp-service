package com.shivamai.otp.repository;

import com.shivamai.otp.entity.OtpRequest;
import com.shivamai.otp.enums.OtpStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OtpRequestRepository
        extends JpaRepository<OtpRequest, Long> {

    Optional<OtpRequest> findTopByIdentifierAndStatusInAndExpiresAtAfterOrderByCreatedAtDesc(
            String identifier,
            List<OtpStatus> statuses,
            LocalDateTime now
    );

    long countByIdentifierAndCreatedAtAfter(String identifier, LocalDateTime time);

}