package com.shivamai.otp.otp.dto.response;

import com.shivamai.otp.otp.enums.OtpPurpose;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtpVerificationResponse {

    private boolean verified;

    private Long requestId;

    private String identifier;

    private String applicationName;

    private OtpPurpose purpose;

    private LocalDateTime verifiedAt;
}