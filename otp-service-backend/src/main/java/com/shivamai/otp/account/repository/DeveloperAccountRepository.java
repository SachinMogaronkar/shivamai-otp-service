package com.shivamai.otp.account.repository;

import com.shivamai.otp.account.entity.DeveloperAccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DeveloperAccountRepository
        extends JpaRepository<DeveloperAccount, Long>,
        JpaSpecificationExecutor<DeveloperAccount> {

    Optional<DeveloperAccount> findByIdentifier(String identifier);

    boolean existsByIdentifier(String identifier);
}