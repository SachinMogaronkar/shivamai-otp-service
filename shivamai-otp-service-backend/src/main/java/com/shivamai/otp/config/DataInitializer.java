package com.shivamai.otp.config;

import com.shivamai.otp.entity.DeveloperClient;
import com.shivamai.otp.enums.ClientStatus;
import com.shivamai.otp.enums.Role;
import com.shivamai.otp.repository.DeveloperClientRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(
            DeveloperClientRepository repository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if(repository.existsByIdentifier("shivamai.otp@gmail.com")) {
                return;
            }

            DeveloperClient admin = new DeveloperClient();

            admin.setIdentifier("shivamai.otp@gmail.com");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setRole(Role.ADMIN);
            admin.setStatus(ClientStatus.ACTIVE);
            admin.setEmailVerified(true);
            admin.setCreatedAt(LocalDateTime.now());

            repository.save(admin);
        };
    }
}