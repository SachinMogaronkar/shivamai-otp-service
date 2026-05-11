package com.shivamai.otp.controller;

import com.shivamai.otp.dtorequest.LoginRequest;
import com.shivamai.otp.dtorequest.OtpResendDTO;
import com.shivamai.otp.dtorequest.OtpVerifyRequest;
import com.shivamai.otp.dtoresponse.ApiResponse;
import com.shivamai.otp.dtoresponse.OtpResponse;
import com.shivamai.otp.entity.DeveloperClient;
import com.shivamai.otp.enums.ClientStatus;
import com.shivamai.otp.enums.OtpType;
import com.shivamai.otp.exception.ClientAuthenticationException;
import com.shivamai.otp.exception.ResourceNotFoundException;
import com.shivamai.otp.exception.UnauthorizedException;
import com.shivamai.otp.repository.DeveloperClientRepository;
import com.shivamai.otp.security.JwtBlacklistService;
import com.shivamai.otp.security.JwtUtil;
import com.shivamai.otp.service.OtpService;
import com.shivamai.otp.service.OtpVerificationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final DeveloperClientRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtBlacklistService jwtBlacklistService;
    private final OtpService otpService;
    private final OtpVerificationService otpVerificationService;

    /* ================= LOGIN ================= */

    @PostMapping("/login")
    public ApiResponse<OtpResponse> login(@Valid @RequestBody LoginRequest request) {

        String identifier = request.getIdentifier();
        if (identifier == null || identifier.isBlank()) {
            throw new ClientAuthenticationException("Identifier required");
        }
        identifier = identifier.trim();

        DeveloperClient user = repository.findByIdentifier(identifier)
                .orElseThrow(() -> new ClientAuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ClientAuthenticationException("Invalid credentials");
        }

        if (user.getStatus() != ClientStatus.ACTIVE) {
            throw new UnauthorizedException("Account not active");
        }

        OtpResponse otpResponse =
                otpService.requestOtp(identifier, OtpType.LOGIN);

        return new ApiResponse<>(
                "SUCCESS",
                "OTP sent",
                otpResponse,
                LocalDateTime.now()
        );
    }

    @PostMapping("/resend-login")
    public ApiResponse<OtpResponse> resendLoginOtp(@RequestBody OtpResendDTO dto) {

        OtpResponse response =
                otpService.resendOtp(dto.getIdentifier(), dto.getRequestId());

        return new ApiResponse<>(
                "SUCCESS",
                "OTP resent",
                response,
                LocalDateTime.now()
        );
    }

    /* ================= VERIFY ================= */

    @PostMapping("/verify-login")
    public ApiResponse<String> verifyLogin(@Valid @RequestBody OtpVerifyRequest request) {

        boolean verified = otpVerificationService.verifyOtp(
                request.getIdentifier(),
                request.getRequestId(),
                request.getOtp()
        );

        if (!verified) {
            throw new ClientAuthenticationException("OTP verification failed");
        }

        DeveloperClient user = repository.findByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != ClientStatus.ACTIVE) {
            throw new UnauthorizedException("Account not active");
        }

        String token = jwtUtil.generateToken(
                user.getIdentifier(),
                user.getRole()
        );

        return new ApiResponse<>(
                "SUCCESS",
                "Login successful",
                token,
                LocalDateTime.now()
        );
    }

    /* ================= LOGOUT ================= */

    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ClientAuthenticationException("Missing token");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validate(token)) {
            throw new ClientAuthenticationException("Invalid token");
        }

        long expiry = jwtUtil.extractExpiration(token).getTime();
        jwtBlacklistService.blacklist(token, expiry);

        return new ApiResponse<>(
                "SUCCESS",
                "Logout successful",
                null,
                LocalDateTime.now()
        );
    }
}