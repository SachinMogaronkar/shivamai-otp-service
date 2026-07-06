package com.shivamai.otp.otp.controller;

import com.shivamai.otp.account.dto.request.OtpVerificationRequest;

import com.shivamai.otp.common.response.ApiResponse;

import com.shivamai.otp.otp.dto.request.OtpRequestDTO;
import com.shivamai.otp.otp.dto.request.OtpResendDTO;

import com.shivamai.otp.otp.dto.response.OtpDeliveryResponse;
import com.shivamai.otp.otp.dto.response.OtpVerificationResponse;

import com.shivamai.otp.otp.service.OtpService;
import com.shivamai.otp.otp.service.OtpVerificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/otp")

@Tag(
        name = "OTP Authentication",
        description = "OTP request and verification APIs"
)

@Slf4j
public class OtpController {

    private final OtpService otpService;

    private final OtpVerificationService verificationService;

    // =====================================
    // REQUEST OTP
    // =====================================

    @PostMapping("/request")
    @Operation(summary = "Request OTP")
    public ApiResponse<OtpDeliveryResponse> requestOtp(
            @Valid
            @RequestBody
            OtpRequestDTO request
    ) {

        log.info(
                "OTP request initiated identifier={}, application={}, purpose={}",
                request.getIdentifier(),
                request.getApplicationName(),
                request.getPurpose()
        );

        OtpDeliveryResponse response =
                otpService.requestOtp(
                        request
                );

        return success(
                "OTP sent successfully",
                response
        );
    }

    // =====================================
    // VERIFY OTP
    // =====================================

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP")
    public ApiResponse<OtpVerificationResponse> verifyOtp(
            @Valid
            @RequestBody
            OtpVerificationRequest request
    ) {

        log.info(
                "OTP verify for identifier={}, requestId={}",
                request.getIdentifier(),
                request.getRequestId()
        );

        OtpVerificationResponse response =
                verificationService.verifyOtp(
                        request.getIdentifier(),
                        request.getRequestId(),
                        request.getOtp()
                );

        return new ApiResponse<>(
                "SUCCESS",
                "OTP verified successfully",
                response,
                LocalDateTime.now()
        );
    }
    // =====================================
    // RESEND OTP
    // =====================================

    @PostMapping("/resend")
    @Operation(summary = "Resend OTP")
    public ApiResponse<OtpDeliveryResponse> resendOtp(
            @Valid
            @RequestBody
            OtpResendDTO request
    ) {

        log.info(
                "OTP resend for identifier={}, requestId={}",
                request.getIdentifier(),
                request.getRequestId()
        );

        OtpDeliveryResponse response =
                otpService.resendOtp(
                        request
                );

        return success(
                "OTP resent successfully",
                response
        );
    }

    // =====================================
    // COMMON SUCCESS RESPONSE
    // =====================================

    private <T> ApiResponse<T> success(
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