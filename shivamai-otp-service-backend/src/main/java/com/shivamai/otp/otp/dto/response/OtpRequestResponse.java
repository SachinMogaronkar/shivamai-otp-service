package com.shivamai.otp.otp.dto.response;

import com.shivamai.otp.otp.enums.OtpChannelType;
import com.shivamai.otp.otp.enums.OtpPurpose;
import com.shivamai.otp.otp.enums.OtpStatus;
import com.shivamai.otp.otp.enums.OtpType;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OtpRequestResponse {

    private Long id;

    private String identifier;

    private String applicationName;

    private OtpStatus status;

    private OtpChannelType channel;

    private OtpType otpType;

    private OtpPurpose purpose;

    private Integer attemptCount;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime verifiedAt;
}