package com.shivamai.otp.dtoresponse;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsageResponse {

    private String clientId;
    private LocalDate date;
    private int otpRequests;
    private int otpVerified;
}
