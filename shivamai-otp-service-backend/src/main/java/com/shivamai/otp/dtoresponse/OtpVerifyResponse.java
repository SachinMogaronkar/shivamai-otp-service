package com.shivamai.otp.dtoresponse;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtpVerifyResponse {
    private boolean verified;
}