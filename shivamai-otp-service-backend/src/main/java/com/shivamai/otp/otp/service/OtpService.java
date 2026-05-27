package com.shivamai.otp.otp.service;

import com.shivamai.otp.account.enums.AccountRole;

import com.shivamai.otp.otp.dto.request.OtpRequestDTO;
import com.shivamai.otp.otp.dto.response.OtpDeliveryResponse;

public interface OtpService {

    OtpDeliveryResponse requestOtp(
            OtpRequestDTO request
    );

    OtpDeliveryResponse resendOtp(
            String identifier,
            Long requestId
    );

    void pingRequest();

    void pingVerification();
}