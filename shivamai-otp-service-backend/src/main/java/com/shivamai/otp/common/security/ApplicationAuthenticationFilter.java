package com.shivamai.otp.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.shivamai.otp.application.entity.DeveloperApplication;
import com.shivamai.otp.application.enums.ApplicationStatus;
import com.shivamai.otp.application.repository.DeveloperApplicationRepository;

import com.shivamai.otp.common.response.ApiResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String CLIENT_ID_HEADER =
            "X-Client-Id";

    private static final String CLIENT_SECRET_HEADER =
            "X-Client-Secret";

    private final DeveloperApplicationRepository appRepository;

    private final PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        if ("OPTIONS".equalsIgnoreCase(
                request.getMethod()
        )) {

            return true;
        }

        String path =
                request.getRequestURI();

        return path == null
                || !path.startsWith("/otp");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientId =
                request.getHeader(
                        CLIENT_ID_HEADER
                );

        String clientSecret =
                request.getHeader(
                        CLIENT_SECRET_HEADER
                );

        // =====================================
        // HEADER VALIDATION
        // =====================================

        if (isBlank(clientId)
                || isBlank(clientSecret)) {

            writeErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing client credentials"
            );

            return;
        }

        // =====================================
        // APPLICATION LOOKUP
        // =====================================

        DeveloperApplication app =
                appRepository.findByClientId(
                                clientId
                        )
                        .orElse(null);

        // =====================================
        // INVALID CREDENTIALS
        // =====================================

        if (app == null
                || app.getClientSecretHash() == null
                || !passwordEncoder.matches(
                clientSecret,
                app.getClientSecretHash()
        )) {

            log.warn(
                    "Invalid client authentication attempt for clientId={}",
                    clientId
            );

            writeErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid client credentials"
            );

            return;
        }

        // =====================================
        // APPLICATION STATUS VALIDATION
        // =====================================

        ApplicationStatus status =
                app.getStatus();

        if (status == null) {

            log.warn(
                    "Application status missing for clientId={}",
                    clientId
            );

            writeErrorResponse(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Application status invalid"
            );

            return;
        }

        switch (status) {

            case REVOKED -> {

                log.warn(
                        "Revoked application access attempt for clientId={}",
                        clientId
                );

                writeErrorResponse(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Application revoked"
                );

                return;
            }

            case SUSPENDED_BY_ADMIN -> {

                log.warn(
                        "Suspended application access attempt for clientId={}",
                        clientId
                );

                writeErrorResponse(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Application suspended"
                );

                return;
            }

            case DISABLED_BY_OWNER -> {

                log.warn(
                        "Disabled application access attempt for clientId={}",
                        clientId
                );

                writeErrorResponse(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Application disabled by owner"
                );

                return;
            }

            case ACTIVE -> {
                // allowed
            }

            default -> {

                log.warn(
                        "Inactive application access attempt for clientId={}",
                        clientId
                );

                writeErrorResponse(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Application inactive"
                );

                return;
            }
        }

        // =====================================
        // REQUEST CONTEXT
        // =====================================

        request.setAttribute(
                "clientId",
                app.getClientId()
        );

        request.setAttribute(
                "appId",
                app.getId()
        );

        request.setAttribute(
                "applicationName",
                app.getApplicationName()
        );

        log.debug(
                "Client authenticated successfully clientId={}",
                clientId
        );

        filterChain.doFilter(
                request,
                response
        );
    }

    // =====================================
    // HELPERS
    // =====================================

    private boolean isBlank(
            String value
    ) {

        return value == null
                || value.isBlank();
    }

    // =====================================
    // ERROR RESPONSE
    // =====================================

    private void writeErrorResponse(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        response.setStatus(
                status
        );

        response.setContentType(
                "application/json"
        );

        ApiResponse<String> apiResponse =
                new ApiResponse<>(
                        "FAILED",
                        message,
                        null,
                        LocalDateTime.now()
                );

        objectMapper.writeValue(
                response.getOutputStream(),
                apiResponse
        );
    }
}