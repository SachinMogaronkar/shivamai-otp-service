package com.shivamai.otp.repository;

import com.shivamai.otp.entity.ClientUsage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClientUsageRepository
        extends JpaRepository<ClientUsage, Long> {

    Optional<ClientUsage> findByClientIdAndDate(String clientId, LocalDate date);

    List<ClientUsage> findByClientId(String clientId);

    long countByClientIdAndCreatedAtAfter(String clientId, LocalDateTime time);

}