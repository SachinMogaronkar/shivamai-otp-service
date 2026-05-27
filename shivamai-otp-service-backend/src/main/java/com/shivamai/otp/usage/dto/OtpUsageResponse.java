package com.shivamai.otp.usage.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpUsageResponse {

    private String clientId;
    private LocalDate date;
    private int otpRequests;
    private int otpVerified;
}
