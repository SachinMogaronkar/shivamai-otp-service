package com.shivamai.otp.channel;

import com.shivamai.otp.enums.OtpChannelType;

public class DeliveryResult {

    private boolean success;
    private OtpChannelType channelUsed;

    public DeliveryResult(boolean success, OtpChannelType channelUsed) {
        this.success = success;
        this.channelUsed = channelUsed;
    }

    public boolean isSuccess() {
        return success;
    }

    public OtpChannelType getChannelUsed() {
        return channelUsed;
    }
}