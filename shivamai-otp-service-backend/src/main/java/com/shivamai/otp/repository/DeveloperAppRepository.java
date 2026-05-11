package com.shivamai.otp.repository;

import com.shivamai.otp.entity.DeveloperApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeveloperAppRepository extends JpaRepository<DeveloperApp, Long> {

    List<DeveloperApp> findByDeveloperId(Long developerId);

    Optional<DeveloperApp> findByClientId(String clientId);
}