package com.shivamai.otp.otp.channel;

import com.shivamai.otp.otp.enums.OtpChannelType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OtpDeliveryResult {

    private final boolean success;

    private final OtpChannelType channelUsed;
}