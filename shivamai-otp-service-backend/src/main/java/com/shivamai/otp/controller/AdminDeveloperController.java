package com.shivamai.otp.controller;

import com.shivamai.otp.dtorequest.EmailRequest;
import com.shivamai.otp.dtoresponse.AdminAppResponse;
import com.shivamai.otp.dtoresponse.ApiResponse;
import com.shivamai.otp.dtoresponse.DeveloperAdminResponse;
import com.shivamai.otp.entity.DeveloperApp;
import com.shivamai.otp.entity.DeveloperClient;
import com.shivamai.otp.entity.WebHookLog;
import com.shivamai.otp.enums.AppStatus;
import com.shivamai.otp.enums.ClientStatus;
import com.shivamai.otp.exception.InvalidRequestException;
import com.shivamai.otp.exception.ResourceNotFoundException;
import com.shivamai.otp.repository.DeveloperAppRepository;
import com.shivamai.otp.repository.DeveloperClientRepository;
import com.shivamai.otp.repository.WebHookLogRepository;
import com.shivamai.otp.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminDeveloperController {

    private final DeveloperClientRepository repository;
    private final DeveloperAppRepository developerAppRepository;
    private final EmailService emailService;
    private final WebHookLogRepository webHookLogRepository;

    // 🔹 Mapper (avoid duplication)
    private DeveloperAdminResponse mapToAdminResponse(DeveloperClient dev) {
        return DeveloperAdminResponse.builder()
                .id(dev.getId())
                .identifier(dev.getIdentifier())
                .status(dev.getStatus().name())
                .emailVerified(dev.isEmailVerified())
                .createdAt(dev.getCreatedAt())
                .build();
    }

    @GetMapping("/developers/pending")
    public ApiResponse<List<DeveloperAdminResponse>> getPendingDevelopers() {

        log.info("Fetching pending developers");

        List<DeveloperAdminResponse> pending =
                repository.findByStatus(ClientStatus.PENDING_ADMIN_APPROVAL)
                        .stream()
                        .map(this::mapToAdminResponse)
                        .toList();

        return new ApiResponse<>(
                "SUCCESS",
                "Pending developers",
                pending,
                LocalDateTime.now()
        );
    }

    @GetMapping("/developers")
    public ApiResponse<List<DeveloperAdminResponse>> getDevelopers() {

        log.info("Fetching all developers");

        List<DeveloperAdminResponse> developers =
                repository.findAll()
                        .stream()
                        .map(this::mapToAdminResponse)
                        .toList();

        return new ApiResponse<>(
                "SUCCESS",
                "Developers retrieved",
                developers,
                LocalDateTime.now()
        );
    }

    @GetMapping("/developers/{id}")
    public ApiResponse<DeveloperAdminResponse> getDeveloper(@PathVariable Long id) {

        log.info("Fetching developer id={}", id);

        DeveloperClient dev =
                repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Developer not found"));

        return new ApiResponse<>(
                "SUCCESS",
                "Developer details",
                mapToAdminResponse(dev),
                LocalDateTime.now()
        );
    }

    @PatchMapping("/developers/{id}/approve")
    public ApiResponse<String> approveDeveloper(@PathVariable Long id) {

        log.info("Approving developer id={}", id);

        DeveloperClient dev =
                repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Developer not found"));

        if (!dev.isEmailVerified()) {
            throw new InvalidRequestException("Developer email not verified");
        }

        if (dev.getStatus() == ClientStatus.ACTIVE) {
            throw new InvalidRequestException("Developer already approved");
        }

        dev.setStatus(ClientStatus.ACTIVE);
        repository.save(dev);

        // 🔹 Send welcome email (non-blocking for approval)
        try {
            Context context = new Context();
            context.setVariable("identifier", dev.getIdentifier());

            EmailRequest request = EmailRequest.builder()
                    .to(dev.getIdentifier())
                    .subject("Welcome to Shivamai OTP Platform")
                    .template("email/developer_welcome")
                    .context(context)
                    .build();

            emailService.send(request);

        } catch (Exception e) {
            log.error("Failed to send welcome email for developer={}", dev.getIdentifier(), e);
        }

        return new ApiResponse<>(
                "SUCCESS",
                "Developer approved",
                null,
                LocalDateTime.now()
        );
    }

    @PatchMapping("/developers/{id}/suspend")
    public ApiResponse<String> suspendDeveloper(@PathVariable Long id) {

        log.info("Suspending developer id={}", id);

        DeveloperClient dev =
                repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Developer not found"));

        if (dev.getStatus() == ClientStatus.SUSPENDED) {
            throw new InvalidRequestException("Developer already suspended");
        }

        dev.setStatus(ClientStatus.SUSPENDED);
        repository.save(dev);

        return new ApiResponse<>(
                "SUCCESS",
                "Developer suspended",
                null,
                LocalDateTime.now()
        );
    }

    @PatchMapping("/apps/{id}/suspend")
    public ApiResponse<String> suspendApp(@PathVariable Long id) {

        log.info("Suspending app id={}", id);

        DeveloperApp app =
                developerAppRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("App not found"));

        if (app.getStatus() == AppStatus.SUSPENDED) {
            throw new InvalidRequestException("App already suspended");
        }

        app.setStatus(AppStatus.SUSPENDED);
        developerAppRepository.save(app);

        return new ApiResponse<>(
                "SUCCESS",
                "App suspended",
                null,
                LocalDateTime.now()
        );
    }

    @PatchMapping("/apps/{id}/activate")
    public ApiResponse<String> activateApp(@PathVariable Long id) {

        log.info("Activating app id={}", id);

        DeveloperApp app =
                developerAppRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("App not found"));

        if (app.getStatus() == AppStatus.ACTIVE) {
            throw new InvalidRequestException("App already active");
        }

        app.setStatus(AppStatus.ACTIVE);
        developerAppRepository.save(app);

        return new ApiResponse<>(
                "SUCCESS",
                "App activated",
                null,
                LocalDateTime.now()
        );
    }

    @GetMapping("/apps/{id}")
    public ApiResponse<AdminAppResponse> getApp(@PathVariable Long id) {

        log.info("Fetching app id={}", id);

        DeveloperApp app =
                developerAppRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("App not found"));

        AdminAppResponse response = AdminAppResponse.builder()
                .id(app.getId())
                .appName(app.getAppName())
                .clientId(app.getClientId())
                .status(app.getStatus())
                .createdAt(app.getCreatedAt())
                .build();

        return new ApiResponse<>(
                "SUCCESS",
                "App details",
                response,
                LocalDateTime.now()
        );
    }

    @GetMapping("/apps")
    public ApiResponse<List<AdminAppResponse>> getAllApps() {

        log.info("Fetching all apps");

        List<AdminAppResponse> apps =
                developerAppRepository.findAll()
                        .stream()
                        .map(app -> AdminAppResponse.builder()
                                .id(app.getId())
                                .appName(app.getAppName())
                                .clientId(app.getClientId())
                                .status(app.getStatus())
                                .createdAt(app.getCreatedAt())
                                .build())
                        .toList();

        return new ApiResponse<>(
                "SUCCESS",
                "All apps retrieved",
                apps,
                LocalDateTime.now()
        );
    }

    @GetMapping("/webhooks")
    public ApiResponse<List<WebHookLog>> getWebhooks() {

        return new ApiResponse<>(
                "SUCCESS",
                "Webhook logs",
                webHookLogRepository.findAll(),
                LocalDateTime.now()
        );
    }
}