package com.shivamai.otp.otp.service;

import com.shivamai.otp.otp.dto.response.OtpVerificationResponse;

public interface OtpVerificationService {

    OtpVerificationResponse verifyOtp(
            String identifier,
            Long requestId,
            String otp
    );

    void ping();
}