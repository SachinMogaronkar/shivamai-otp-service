package com.shivamai.otp.controller;

import com.shivamai.otp.dtorequest.CreateAppRequest;
import com.shivamai.otp.dtoresponse.ApiResponse;
import com.shivamai.otp.dtoresponse.DeveloperAppResponse;
import com.shivamai.otp.dtoresponse.DeveloperProfileResponse;
import com.shivamai.otp.dtoresponse.DeveloperAppView;
import com.shivamai.otp.entity.DeveloperApp;
import com.shivamai.otp.entity.DeveloperClient;
import com.shivamai.otp.enums.AppStatus;
import com.shivamai.otp.exception.ResourceNotFoundException;
import com.shivamai.otp.exception.UnauthorizedException;
import com.shivamai.otp.repository.DeveloperAppRepository;
import com.shivamai.otp.repository.DeveloperClientRepository;
import com.shivamai.otp.util.ClientCredentialGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/developer/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DeveloperDashboardController {

    private final DeveloperClientRepository developerRepository;
    private final DeveloperAppRepository appRepository;
    private final PasswordEncoder passwordEncoder;

    private DeveloperClient getCurrentDeveloper() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }

        String identifier = auth.getName();

        return developerRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found"));
    }

    private void validateOwnership(DeveloperApp app, DeveloperClient dev) {
        if (!app.getDeveloperId().equals(dev.getId())) {
            throw new UnauthorizedException("You do not own this app");
        }
    }

    @GetMapping("/profile")
    public ApiResponse<DeveloperProfileResponse> getProfile() {

        DeveloperClient developer = getCurrentDeveloper();

        log.info("Fetching profile for developer={}", developer.getIdentifier());

        DeveloperProfileResponse response = DeveloperProfileResponse.builder()
                .identifier(developer.getIdentifier())
                .emailVerified(developer.isEmailVerified())
                .status(developer.getStatus().name())
                .createdAt(developer.getCreatedAt())
                .build();

        return new ApiResponse<>(
                "SUCCESS",
                "Developer profile",
                response,
                LocalDateTime.now()
        );
    }

    @PostMapping("/apps")
    public ApiResponse<DeveloperAppResponse> createApp(
            @Valid @RequestBody CreateAppRequest request) {

        DeveloperClient developer = getCurrentDeveloper();

        log.info("Creating app for developer={}", developer.getIdentifier());

        String clientId = ClientCredentialGenerator.generateClientId();
        String secret = ClientCredentialGenerator.generateClientSecret();

        DeveloperApp app = DeveloperApp.builder()
                .developerId(developer.getId())
                .appName(request.getAppName())
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(secret))
                .webhookUrl(request.getWebhookUrl())
                .status(AppStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        appRepository.save(app);

        return new ApiResponse<>(
                "SUCCESS",
                "Application created",
                new DeveloperAppResponse(clientId, secret),
                LocalDateTime.now()
        );
    }

    @GetMapping("/apps")
    public ApiResponse<List<DeveloperAppView>> getApps() {

        DeveloperClient developer = getCurrentDeveloper();

        log.info("Fetching apps for developer={}", developer.getIdentifier());

        List<DeveloperAppView> apps =
                appRepository.findByDeveloperId(developer.getId())
                        .stream()
                        .map(app -> DeveloperAppView.builder()
                                .id(app.getId())
                                .appName(app.getAppName())
                                .clientId(app.getClientId())
                                .status(app.getStatus().name())
                                .createdAt(app.getCreatedAt())
                                .build())
                        .toList();

        return new ApiResponse<>(
                "SUCCESS",
                "Developer apps",
                apps,
                LocalDateTime.now()
        );
    }

    @DeleteMapping("/apps/{id}")
    public ApiResponse<String> deleteApp(@PathVariable Long id) {

        DeveloperClient developer = getCurrentDeveloper();

        log.info("Deleting app id={} by developer={}", id, developer.getIdentifier());

        DeveloperApp app =
                appRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("App not found"));

        validateOwnership(app, developer);

        appRepository.delete(app);

        return new ApiResponse<>(
                "SUCCESS",
                "App deleted",
                null,
                LocalDateTime.now()
        );
    }

    @PatchMapping("/apps/{id}/rotate-secret")
    public ApiResponse<String> rotateSecret(@PathVariable Long id) {

        DeveloperClient developer = getCurrentDeveloper();

        log.info("Rotating secret for app id={} by developer={}", id, developer.getIdentifier());

        DeveloperApp app =
                appRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("App not found"));

        validateOwnership(app, developer);

        String newSecret = ClientCredentialGenerator.generateClientSecret();

        app.setClientSecret(passwordEncoder.encode(newSecret));

        appRepository.save(app);

        return new ApiResponse<>(
                "SUCCESS",
                "Secret rotated",
                newSecret,
                LocalDateTime.now()
        );
    }
}