package com.shivamai.otp.service;

import com.shivamai.otp.dtoresponse.OtpResponse;
import com.shivamai.otp.enums.OtpChannelType;
import com.shivamai.otp.enums.OtpType;

public interface OtpService {

    OtpResponse requestOtp(String identifier, OtpType type);

    OtpResponse resendOtp(String identifier, Long requestId);

    void pingRequest();

    void pingVerification();
}
