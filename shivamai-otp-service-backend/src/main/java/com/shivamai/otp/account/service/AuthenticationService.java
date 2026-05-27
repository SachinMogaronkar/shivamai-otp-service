package com.shivamai.otp.account.service;

import com.shivamai.otp.account.dto.request.DeveloperRegisterRequest;
import com.shivamai.otp.account.dto.request.LoginRequest;
import com.shivamai.otp.account.dto.request.OtpVerificationRequest;
import com.shivamai.otp.account.dto.response.AuthenticationResponse;
import com.shivamai.otp.otp.dto.response.OtpDeliveryResponse;
import com.shivamai.otp.account.dto.response.RegistrationInitiationResponse;
import com.shivamai.otp.account.dto.response.RegistrationVerificationResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {

    // =====================================
    // REGISTRATION
    // =====================================

    RegistrationInitiationResponse registerDeveloper(DeveloperRegisterRequest request);

    RegistrationVerificationResponse verifyRegistrationOtp(OtpVerificationRequest request);

    // =====================================
    // LOGIN
    // =====================================

    OtpDeliveryResponse loginDeveloper(LoginRequest request);

    AuthenticationResponse verifyLoginOtp(OtpVerificationRequest request);

    // =====================================
    // LOGOUT
    // =====================================

    void logoutDeveloper(HttpServletRequest request);
}