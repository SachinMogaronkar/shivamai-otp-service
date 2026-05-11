package com.shivamai.otp.repository;

import com.shivamai.otp.entity.DeveloperClient;
import com.shivamai.otp.enums.ClientStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeveloperClientRepository
        extends JpaRepository<DeveloperClient, Long> {

    Optional<DeveloperClient> findByIdentifier(String identifier);

    boolean existsByIdentifier(String identifier);

    List<DeveloperClient> findByStatus(ClientStatus status);
}