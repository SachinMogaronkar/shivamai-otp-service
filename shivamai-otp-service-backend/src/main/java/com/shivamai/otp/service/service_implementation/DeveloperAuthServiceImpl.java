package com.shivamai.otp.service.service_implementation;

import com.shivamai.otp.dtorequest.LoginRequest;
import com.shivamai.otp.dtorequest.DeveloperRegisterRequest;
import com.shivamai.otp.dtorequest.OtpVerifyRequest;
import com.shivamai.otp.dtoresponse.DeveloperLoginResponse;
import com.shivamai.otp.entity.DeveloperClient;
import com.shivamai.otp.enums.ClientStatus;
import com.shivamai.otp.enums.OtpType;
import com.shivamai.otp.enums.Role;
import com.shivamai.otp.exception.OtpException;
import com.shivamai.otp.repository.DeveloperClientRepository;
import com.shivamai.otp.security.DeveloperJwtService;
import com.shivamai.otp.service.DeveloperAuthService;
import com.shivamai.otp.service.OtpService;
import com.shivamai.otp.service.OtpVerificationService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeveloperAuthServiceImpl implements DeveloperAuthService {

    private static final Logger log = LoggerFactory.getLogger(DeveloperAuthServiceImpl.class);

    private final DeveloperClientRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final OtpVerificationService verificationService;
    private final DeveloperJwtService developerJwtService;

    // 🔴 FIX 1 — Transactional (important)
    @Override
    @Transactional
    public void registerDeveloper(DeveloperRegisterRequest request) {

        log.info("Registering developer: {}", request.getIdentifier());

        if (repository.existsByIdentifier(request.getIdentifier())) {
            throw new OtpException("Identifier already registered");
        }

        DeveloperClient dev = new DeveloperClient();

        dev.setIdentifier(request.getIdentifier());
        dev.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        dev.setRole(Role.DEVELOPER);
        dev.setEmailVerified(false);
        dev.setStatus(ClientStatus.PENDING_ADMIN_APPROVAL);
        dev.setCreatedAt(LocalDateTime.now());

        repository.save(dev);

        var otpResponse =
                otpService.requestOtp(
                        request.getIdentifier(),
                        OtpType.REGISTRATION
                );

        dev.setEmailVerificationRequestId(otpResponse.getRequestId());

        repository.save(dev);
    }

    @Override
    public void verifyRegistrationOtp(OtpVerifyRequest request) {

        DeveloperClient dev =
                repository.findByIdentifier(request.getIdentifier())
                        .orElseThrow(() -> new OtpException("Developer not found"));

        // 🔴 FIX 2 — null safety
        if (dev.getEmailVerificationRequestId() == null) {
            throw new OtpException("No OTP request found for verification");
        }

        // 🔴 FIX 3 — prevent double verification
        if (dev.isEmailVerified()) {
            throw new OtpException("Email already verified");
        }

        verificationService.verifyOtp(
                request.getIdentifier(),
                dev.getEmailVerificationRequestId(),
                request.getOtp()
        );

        dev.setEmailVerified(true);

        repository.save(dev);
    }

    @Override
    public void loginDeveloper(LoginRequest request) {

        log.info("Login attempt for: {}", request.getIdentifier());

        DeveloperClient dev =
                repository.findByIdentifier(request.getIdentifier())
                        .orElseThrow(() -> new OtpException("Developer not found"));

        if (!passwordEncoder.matches(request.getPassword(), dev.getPasswordHash())) {
            throw new OtpException("Invalid credentials");
        }

        if (!dev.isEmailVerified()) {
            throw new OtpException("Identifier not verified");
        }

        if (dev.getStatus() != ClientStatus.ACTIVE) {
            throw new OtpException("Developer not approved yet");
        }

        // 🔴 FIX 4 — reset previous login OTP
        dev.setLoginOtpRequestId(null);

        var otpResponse =
                otpService.requestOtp(
                        request.getIdentifier(),
                        OtpType.LOGIN
                );

        dev.setLoginOtpRequestId(otpResponse.getRequestId());

        repository.save(dev);
    }

    @Override
    public DeveloperLoginResponse verifyLoginOtp(OtpVerifyRequest request) {

        DeveloperClient dev =
                repository.findByIdentifier(request.getIdentifier())
                        .orElseThrow(() -> new OtpException("Developer not found"));

        // 🔴 FIX 5 — null safety
        if (dev.getLoginOtpRequestId() == null) {
            throw new OtpException("No login OTP requested");
        }

        verificationService.verifyOtp(
                request.getIdentifier(),
                dev.getLoginOtpRequestId(),
                request.getOtp()
        );

        // 🔴 FIX 6 — defensive validation
        if (!dev.isEmailVerified() || dev.getStatus() != ClientStatus.ACTIVE) {
            throw new OtpException("User not eligible for login");
        }

        log.info("Login OTP verified for: {}", request.getIdentifier());

        String token = developerJwtService.generateToken(dev);

        return new DeveloperLoginResponse(token);
    }
}