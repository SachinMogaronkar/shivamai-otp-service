package com.shivamai.otp.dtorequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerifyRequest {

    private String identifier;

    private Long requestId;

    private String otp;
}