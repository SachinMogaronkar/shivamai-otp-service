package com.shivamai.otp.account.service;

import com.shivamai.otp.account.dto.request.DeveloperRegisterRequest;
import com.shivamai.otp.account.dto.request.LoginRequest;
import com.shivamai.otp.account.dto.request.OtpVerificationRequest;

import com.shivamai.otp.account.dto.response.AuthenticationResponse;
import com.shivamai.otp.account.dto.response.RegistrationInitiationResponse;
import com.shivamai.otp.account.dto.response.RegistrationVerificationResponse;

import com.shivamai.otp.account.entity.DeveloperAccount;

import com.shivamai.otp.account.enums.AccountRole;
import com.shivamai.otp.account.enums.DeveloperAccountStatus;

import com.shivamai.otp.account.repository.DeveloperAccountRepository;

import com.shivamai.otp.account.security.DeveloperJwtService;

import com.shivamai.otp.audit.enums.AuditEventType;
import com.shivamai.otp.audit.service.AuditService;

import com.shivamai.otp.common.exception.OtpException;

import com.shivamai.otp.common.security.JwtBlacklistService;
import com.shivamai.otp.common.security.JwtUtil;

import com.shivamai.otp.otp.dto.request.OtpRequestDTO;
import com.shivamai.otp.otp.dto.response.OtpDeliveryResponse;

import com.shivamai.otp.otp.enums.OtpPurpose;

import com.shivamai.otp.otp.service.OtpService;
import com.shivamai.otp.otp.service.OtpVerificationService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.shivamai.otp.account.enums.AccountRole.DEVELOPER;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthenticationServiceImpl
        implements AuthenticationService {

    private final DeveloperAccountRepository repository;

    private final PasswordEncoder passwordEncoder;

    private final OtpService otpService;

    private final OtpVerificationService verificationService;

    private final DeveloperJwtService developerJwtService;

    private final JwtUtil jwtUtil;

    private final JwtBlacklistService jwtBlacklistService;

    private final AuditService auditService;

    // =====================================
    // REGISTER DEVELOPER
    // =====================================

    @Override
    public RegistrationInitiationResponse registerDeveloper(
            DeveloperRegisterRequest request
    ) {

        log.info(
                "Registering developer={}",
                request.getIdentifier()
        );

        if (repository.existsByIdentifier(
                request.getIdentifier()
        )) {

            auditService.logAccountEvent(
                    request.getIdentifier(),
                    request.getIdentifier(),
                    "/auth/register",
                    AuditEventType.REGISTRATION_FAILED,
                    409
            );

            throw new OtpException(
                    "User mail already registered"
            );
        }

        DeveloperAccount developer =
                new DeveloperAccount();

        developer.setFullName(
                request.getFullName().trim()
        );

        developer.setIdentifier(
                request.getIdentifier()
        );

        developer.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        developer.setAccountRole(
                DEVELOPER
        );

        developer.setStatus(
                DeveloperAccountStatus.REGISTERED
        );

        developer.setCreatedAt(
                LocalDateTime.now()
        );

        DeveloperAccount savedDeveloper =
                repository.save(
                        developer
                );

        OtpDeliveryResponse otpResponse =
                otpService.requestOtp(
                        OtpRequestDTO.builder()
                                .identifier(
                                        request.getIdentifier()
                                )
                                .fullName(
                                        request.getFullName()
                                )
                                .applicationName(
                                        resolveApplicationName(developer)
                                )
                                .purpose(
                                        OtpPurpose.REGISTRATION
                                )
                                .accountRole(developer.getAccountRole())
                                .build()
                );

        savedDeveloper.setEmailVerificationRequestId(
                otpResponse.getRequestId()
        );

        auditService.logAccountEvent(
                savedDeveloper.getIdentifier(),
                savedDeveloper.getIdentifier(),
                "/auth/register",
                AuditEventType.DEVELOPER_REGISTERED,
                201
        );

        log.info(
                "Developer registration initiated={}",
                request.getIdentifier()
        );

        return RegistrationInitiationResponse.builder()
                .identifier(
                        savedDeveloper.getIdentifier()
                )
                .requestId(
                        otpResponse.getRequestId()
                )
                .expiresAt(
                        otpResponse.getExpiresAt()
                )
                .remainingSeconds(
                        otpResponse.getRemainingSeconds()
                )
                .channel(
                        otpResponse.getChannel().name()
                )
                .status(
                        savedDeveloper.getStatus().name()
                )
                .build();
    }

    // =====================================
    // VERIFY REGISTRATION OTP
    // =====================================

    @Override
    public RegistrationVerificationResponse verifyRegistrationOtp(
            OtpVerificationRequest request
    ) {

        DeveloperAccount developer =
                getAccountOrThrow(
                        request.getIdentifier()
                );

        if (developer.getEmailVerificationRequestId()
                == null) {

            throw new OtpException(
                    "No OTP request found"
            );
        }

        if (developer.getStatus()
                != DeveloperAccountStatus.REGISTERED) {

            throw new OtpException(
                    "Email already verified"
            );
        }

        verificationService.verifyOtp(
                request.getIdentifier(),
                developer.getEmailVerificationRequestId(),
                request.getOtp()
        );

        developer.setStatus(
                DeveloperAccountStatus.PENDING_ADMIN_APPROVAL
        );

        developer.setEmailVerificationRequestId(
                null
        );

        auditService.logAccountEvent(
                developer.getIdentifier(),
                developer.getIdentifier(),
                "/auth/verify-registration",
                AuditEventType.OTP_VERIFIED,
                200
        );

        log.info(
                "Developer verification successful={}",
                request.getIdentifier()
        );

        return RegistrationVerificationResponse.builder()
                .fullName(
                        developer.getFullName()
                )
                .identifier(
                        developer.getIdentifier()
                )
                .status(
                        developer.getStatus()
                )
                .build();
    }

    // =====================================
    // LOGIN
    // =====================================

    @Override
    public OtpDeliveryResponse loginAccount(
            LoginRequest request
    ) {

        log.info(
                "Login attempt={}",
                request.getIdentifier()
        );

        DeveloperAccount developer =
                getAccountOrThrow(
                        request.getIdentifier()
                );
//
        log.info(
                "Developer loaded -> fullName='{}'",
                developer.getFullName()
        );


        if (!passwordEncoder.matches(
                request.getPassword(),
                developer.getPasswordHash()
        )) {

            auditService.logAccountEvent(
                    request.getIdentifier(),
                    request.getIdentifier(),
                    "/auth/login",
                    AuditEventType.LOGIN_FAILED,
                    401
            );

            throw new OtpException(
                    "Invalid credentials"
            );
        }

        validateLoginEligibility(
                developer
        );

        OtpPurpose purpose =
                resolveLoginPurpose(
                        developer
                );

        OtpDeliveryResponse otpResponse =
                otpService.requestOtp(
                        OtpRequestDTO.builder()
                                .identifier(
                                        request.getIdentifier()
                                )
                                .fullName(
                                        developer.getFullName()
                                )
                                .applicationName(
                                        resolveApplicationName(developer)
                                )
                                .purpose(
                                        purpose
                                )
                                .accountRole(developer.getAccountRole())
                                .build()
                );

        developer.setLoginOtpRequestId(
                otpResponse.getRequestId()
        );

        auditService.logAccountEvent(
                developer.getIdentifier(),
                developer.getIdentifier(),
                "/auth/login",
                AuditEventType.LOGIN_INITIATED,
                200
        );

        log.info(
                "Login OTP issued={}",
                request.getIdentifier()
        );

        return otpResponse;
    }

    // =====================================
    // VERIFY LOGIN OTP
    // =====================================

    @Override
    public AuthenticationResponse verifyLoginOtp(
            OtpVerificationRequest request
    ) {

        DeveloperAccount developer =
                getAccountOrThrow(
                        request.getIdentifier()
                );

        if (developer.getLoginOtpRequestId()
                == null) {

            throw new OtpException(
                    "No login OTP requested"
            );
        }

        verificationService.verifyOtp(
                request.getIdentifier(),
                developer.getLoginOtpRequestId(),
                request.getOtp()
        );

        validateLoginEligibility(
                developer
        );

        developer.setLoginOtpRequestId(
                null
        );

        String token =
                developerJwtService.generateToken(
                        developer
                );

        auditService.logAccountEvent(
                developer.getIdentifier(),
                developer.getIdentifier(),
                "/auth/verify-login",
                AuditEventType.LOGIN_SUCCESS,
                200
        );

        log.info(
                "Login successful={}",
                developer.getIdentifier()
        );

        return new AuthenticationResponse(
                token
        );
    }

    // =====================================
    // LOGOUT
    // =====================================

    @Override
    public void logoutDeveloper(
            HttpServletRequest request
    ) {

        String authHeader =
                request.getHeader(
                        "Authorization"
                );

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            throw new OtpException(
                    "Missing token"
            );
        }

        String token =
                authHeader.substring(7);

        if (!jwtUtil.validate(token)) {

            throw new OtpException(
                    "Invalid token"
            );
        }

        if (jwtBlacklistService.isBlacklisted(
                token
        )) {

            throw new OtpException(
                    "Token already invalidated"
            );
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new OtpException(
                    "Unauthorized"
            );
        }

        String identifier =
                authentication.getName();

        long expiry =
                jwtUtil.extractExpiration(token)
                        .getTime();

        jwtBlacklistService.blacklist(
                token,
                expiry
        );

        SecurityContextHolder.clearContext();

        auditService.logAccountEvent(
                identifier,
                identifier,
                "/auth/logout",
                AuditEventType.LOGOUT,
                200
        );

        log.info(
                "Developer logout successful={}",
                identifier
        );
    }

    // =====================================
    // INTERNAL HELPERS
    // =====================================

    private OtpPurpose resolveLoginPurpose(
            DeveloperAccount account
    ) {

        return switch (
                account.getAccountRole()
                ) {

            case ADMIN ->
                    OtpPurpose.ADMIN_LOGIN;

            case DEVELOPER ->
                    OtpPurpose.DEVELOPER_LOGIN;
        };
    }

    private String resolveApplicationName(
            DeveloperAccount account
    ) {

        return switch (
                account.getAccountRole()
                ) {

            case ADMIN ->
                    "Shivamai Admin Console";

            case DEVELOPER ->
                    "Shivamai Developer Console";
        };
    }

    private DeveloperAccount getAccountOrThrow(
            String identifier
    ) {

        return repository.findByIdentifier(
                        identifier
                )
                .orElseThrow(
                        () -> new OtpException(
                                "Account not found"
                        )
                );
    }

    private void validateLoginEligibility(
            DeveloperAccount developer
    ) {

        switch (developer.getStatus()) {

            case REGISTERED -> {

                auditLoginFailure(
                        developer,
                        "Email verification pending"
                );

                throw new OtpException(
                        "Email verification pending"
                );
            }

            case PENDING_ADMIN_APPROVAL -> {

                auditLoginFailure(
                        developer,
                        "Developer approval pending"
                );

                throw new OtpException(
                        "Developer approval pending"
                );
            }

            case SUSPENDED_BY_ADMIN -> {

                auditLoginFailure(
                        developer,
                        "Developer account suspended"
                );

                throw new OtpException(
                        "Developer account suspended"
                );
            }

            case REVOKED -> {

                auditLoginFailure(
                        developer,
                        "Developer access revoked"
                );

                throw new OtpException(
                        "Developer access revoked"
                );
            }

            case ACTIVE -> {
                return;
            }

            default -> {

                auditLoginFailure(
                        developer,
                        "Developer account is not active"
                );

                throw new OtpException(
                        "Developer account is not active"
                );
            }
        }
    }

    private void auditLoginFailure(
            DeveloperAccount developer,
            String reason
    ) {

        auditService.logAccountEvent(
                developer.getIdentifier(),
                developer.getIdentifier(),
                "/auth/login",
                AuditEventType.LOGIN_FAILED,
                403
        );

        log.warn(
                "Login blocked for developer={} reason={}",
                developer.getIdentifier(),
                reason
        );
    }
}