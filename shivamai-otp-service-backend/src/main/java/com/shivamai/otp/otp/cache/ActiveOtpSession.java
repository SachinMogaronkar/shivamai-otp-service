package com.shivamai.otp.otp.cache;

import com.shivamai.otp.otp.enums.OtpPurpose;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveOtpSession {

    private Long requestId;

    private String otp;

    private String otpHash;

    private long expiryTime;

    private String identifier;

    private String applicationName;

    private String displayName;

    private OtpPurpose purpose;
}