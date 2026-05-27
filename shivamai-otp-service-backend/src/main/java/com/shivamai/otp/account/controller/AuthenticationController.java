package com.shivamai.otp.account.controller;

import com.shivamai.otp.account.dto.request.DeveloperRegisterRequest;
import com.shivamai.otp.account.dto.request.LoginRequest;
import com.shivamai.otp.account.dto.request.OtpVerificationRequest;

import com.shivamai.otp.account.dto.response.AuthenticationResponse;
import com.shivamai.otp.account.dto.response.RegistrationInitiationResponse;
import com.shivamai.otp.account.dto.response.RegistrationVerificationResponse;

import com.shivamai.otp.common.response.ApiResponse;

import com.shivamai.otp.otp.dto.request.OtpResendDTO;
import com.shivamai.otp.otp.dto.response.OtpDeliveryResponse;

import com.shivamai.otp.account.service.AuthenticationService;

import com.shivamai.otp.otp.service.OtpService;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")

@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService
            authenticationService;

    private final OtpService
            otpService;

    // =====================================
    // REGISTER
    // =====================================

    @PostMapping("/register")
    public ApiResponse<RegistrationInitiationResponse>
    register(
            @Valid
            @RequestBody
            DeveloperRegisterRequest request
    ) {

        return success(
                "Registration OTP sent",
                authenticationService
                        .registerDeveloper(request)
        );
    }

    // =====================================
    // VERIFY REGISTRATION
    // =====================================

    @PostMapping("/verify-registration")
    public ApiResponse<RegistrationVerificationResponse>
    verifyRegistration(
            @Valid
            @RequestBody
            OtpVerificationRequest request
    ) {

        return success(
                "Registration verified successfully",
                authenticationService
                        .verifyRegistrationOtp(request)
        );
    }

    // =====================================
    // LOGIN
    // =====================================

    @PostMapping("/login")
    public ApiResponse<OtpDeliveryResponse>
    login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {

        return success(
                "OTP sent",
                authenticationService
                        .loginDeveloper(request)
        );
    }

    // =====================================
    // RESEND LOGIN OTP
    // =====================================

    @PostMapping("/resend-login")
    public ApiResponse<OtpDeliveryResponse>
    resendLoginOtp(
            @Valid
            @RequestBody
            OtpResendDTO request
    ) {

        return success(
                "OTP resent",
                otpService.resendOtp(
                        request.getIdentifier(),
                        request.getRequestId()
                )
        );
    }

    // =====================================
    // VERIFY LOGIN
    // =====================================

    @PostMapping("/verify-login")
    public ApiResponse<AuthenticationResponse>
    verifyLogin(
            @Valid
            @RequestBody
            OtpVerificationRequest request
    ) {

        return success(
                "Login successful",
                authenticationService
                        .verifyLoginOtp(request)
        );
    }

    // =====================================
    // LOGOUT
    // =====================================

    @PostMapping("/logout")
    public ApiResponse<Void>
    logout(
            HttpServletRequest request
    ) {

        authenticationService.logoutDeveloper(
                request
        );

        return success(
                "Logout successful",
                null
        );
    }

    // =====================================
    // COMMON SUCCESS RESPONSE
    // =====================================

    private <T> ApiResponse<T>
    success(
            String message,
            T data
    ) {

        return new ApiResponse<>(
                "SUCCESS",
                message,
                data,
                LocalDateTime.now()
        );
    }
}