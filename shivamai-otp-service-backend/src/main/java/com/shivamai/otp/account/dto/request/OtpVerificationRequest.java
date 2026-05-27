package com.shivamai.otp.account.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerificationRequest {

    private String identifier;

    private Long requestId;

    private String otp;
}