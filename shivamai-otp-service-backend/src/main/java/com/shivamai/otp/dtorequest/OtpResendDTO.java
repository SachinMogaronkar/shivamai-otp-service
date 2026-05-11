package com.shivamai.otp.dtorequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpResendDTO {

    private String identifier;
    private Long requestId;
}