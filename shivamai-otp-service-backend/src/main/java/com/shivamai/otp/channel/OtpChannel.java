package com.shivamai.otp.channel;

import com.shivamai.otp.enums.OtpType;

public interface OtpChannel {

    boolean send(String identifier, String otp, OtpType type, int expirySeconds);

    String getChannelName();

    void ping();
}