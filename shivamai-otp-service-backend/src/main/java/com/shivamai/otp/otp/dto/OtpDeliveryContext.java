package com.shivamai.otp.otp.dto;

import com.shivamai.otp.account.enums.AccountRole;

import com.shivamai.otp.otp.enums.OtpPurpose;
import com.shivamai.otp.otp.enums.OtpType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OtpDeliveryContext {

    private final String identifier;

    private final String otp;

    private final String displayName;

    private final String applicationName;

    private final OtpPurpose purpose;

    private final OtpType otpType;

    private final AccountRole accountRole;

    private final int expirySeconds;
}