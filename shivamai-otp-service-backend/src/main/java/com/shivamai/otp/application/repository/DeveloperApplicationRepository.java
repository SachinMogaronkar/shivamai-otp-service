package com.shivamai.otp.application.repository;

import com.shivamai.otp.application.dto.response.DeveloperApplicationSummaryResponse;
import com.shivamai.otp.application.entity.DeveloperApplication;

import com.shivamai.otp.application.enums.ApplicationStatus;
import com.shivamai.otp.common.pagination.PageQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DeveloperApplicationRepository
        extends JpaRepository<DeveloperApplication, Long>,
        JpaSpecificationExecutor<DeveloperApplication> {

    List<DeveloperApplication> findByDeveloperId(Long developerId);

    Optional<DeveloperApplication> findByIdAndDeveloperId(Long id, Long developerId);

    Optional<DeveloperApplication> findByClientId(String clientId);

    boolean existsByClientId(String clientId);

    boolean existsByDeveloperIdAndApplicationName(Long developerId, String applicationName);

    long countByDeveloperIdAndStatus(Long developerId, ApplicationStatus status);
}