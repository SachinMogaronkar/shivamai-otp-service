package com.shivamai.otp.common.config;

import com.shivamai.otp.account.entity.DeveloperAccount;

import com.shivamai.otp.account.enums.DeveloperAccountStatus;
import com.shivamai.otp.account.enums.AccountRole;

import com.shivamai.otp.account.repository.DeveloperAccountRepository;

import org.springframework.boot.CommandLineRunner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class SystemDataInitializer {

    @Bean
    CommandLineRunner initAdmin(
            DeveloperAccountRepository repository,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            if (repository.existsByIdentifier(
                    "shivamai.otp@gmail.com"
            )) {

                return;
            }

            DeveloperAccount admin =
                    new DeveloperAccount();

            admin.setFullName(
                    "ShivaMai Administrator"
            );

            admin.setIdentifier(
                    "shivamai.otp@gmail.com"
            );

            admin.setPasswordHash(
                    passwordEncoder.encode(
                            "Admin@123"
                    )
            );

            admin.setAccountRole(
                    AccountRole.ADMIN
            );

            admin.setStatus(
                    DeveloperAccountStatus.ACTIVE
            );

            admin.setCreatedAt(
                    LocalDateTime.now()
            );

            repository.save(admin);
        };
    }
}