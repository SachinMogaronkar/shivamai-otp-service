package com.shivamai.otp.controller;

import com.shivamai.otp.dtorequest.OtpRequestDTO;
import com.shivamai.otp.dtorequest.OtpResendDTO;
import com.shivamai.otp.dtorequest.OtpVerifyRequest;
import com.shivamai.otp.dtoresponse.ApiResponse;
import com.shivamai.otp.dtoresponse.OtpResponse;
import com.shivamai.otp.dtoresponse.OtpVerifyResponse;
import com.shivamai.otp.service.OtpService;
import com.shivamai.otp.service.service_implementation.OtpServiceImpl;
import com.shivamai.otp.service.OtpVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/otp")
@Tag(name = "OTP Authentication", description = "OTP request and verification APIs")
@Slf4j
public class OtpController {

    private final OtpService otpService;
    private final OtpVerificationService verificationService;

    @PostMapping("/request")
    @Operation(summary = "Request OTP")
    public ApiResponse<OtpResponse> requestOtp(@Valid @RequestBody OtpRequestDTO dto) {

        log.info("OTP request for identifier={}", dto.getIdentifier());

        OtpResponse response =
                otpService.requestOtp(dto.getIdentifier(), dto.getType());

        return new ApiResponse<>(
                "SUCCESS",
                "OTP sent successfully",
                response,
                LocalDateTime.now()
        );
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP")
    public ApiResponse<OtpVerifyResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest dto) {

        log.info("OTP verify for identifier={}, requestId={}",
                dto.getIdentifier(), dto.getRequestId());

        verificationService.verifyOtp(
                dto.getIdentifier(),
                dto.getRequestId(),
                dto.getOtp()
        );

        return new ApiResponse<>(
                "SUCCESS",
                "OTP verified successfully",
                OtpVerifyResponse.builder()
                        .verified(true)
                        .build(),
                LocalDateTime.now()
        );
    }

    @PostMapping("/resend")
    @Operation(summary = "Resend OTP")
    public ApiResponse<OtpResponse> resendOtp(@Valid @RequestBody OtpResendDTO dto) {

        log.info("OTP resend for identifier={}, requestId={}",
                dto.getIdentifier(), dto.getRequestId());

        OtpResponse response =
                otpService.resendOtp(dto.getIdentifier(), dto.getRequestId());

        return new ApiResponse<>(
                "SUCCESS",
                "OTP resent successfully",
                response,
                LocalDateTime.now()
        );
    }
}