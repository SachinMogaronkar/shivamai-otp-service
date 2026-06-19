package com.shivamai.otp.otp.service;

import com.shivamai.otp.otp.entity.OtpRequest;

public interface OtpAttemptService {

    int recordFailedAttempt(
            OtpRequest otpRequest
    );

    void blockOtp(OtpRequest otpRequest);
}