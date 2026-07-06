package com.shivamai.otp.otp.dto;

import com.shivamai.otp.account.enums.AccountRole;

import com.shivamai.otp.otp.enums.OtpChannelType;
import com.shivamai.otp.otp.enums.OtpPurpose;
import com.shivamai.otp.otp.enums.OtpType;

import lombok.Builder;
import lombok.Getter;
import org.thymeleaf.context.Context;

@Getter
@Builder
public class OtpDeliveryContext {

    private final String identifier;

    private final String otp;

    private final String fullName;

    private final String applicationName;

    private final OtpPurpose purpose;

    private final OtpType otpType;

    private OtpChannelType channelType;

    private final AccountRole accountRole;

    private final int expirySeconds;

    private String subject;

    private String template;

    private Context context;
}