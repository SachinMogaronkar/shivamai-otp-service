package com.shivamai.otp.service;

import com.shivamai.otp.entity.OtpRequest;

public interface WebHookService {

    void sendVerificationEvent(String url, OtpRequest request);
}