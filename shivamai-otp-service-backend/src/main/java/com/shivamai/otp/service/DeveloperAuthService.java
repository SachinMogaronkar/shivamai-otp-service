package com.shivamai.otp.service;

import com.shivamai.otp.dtorequest.DeveloperRegisterRequest;
import com.shivamai.otp.dtorequest.LoginRequest;
import com.shivamai.otp.dtorequest.OtpVerifyRequest;
import com.shivamai.otp.dtoresponse.DeveloperLoginResponse;

public interface DeveloperAuthService {

    void registerDeveloper(DeveloperRegisterRequest request);

    void verifyRegistrationOtp(OtpVerifyRequest request);

    void loginDeveloper(LoginRequest request);

    DeveloperLoginResponse verifyLoginOtp(OtpVerifyRequest request);
}