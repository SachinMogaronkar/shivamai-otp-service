package com.shivamai.otp.otp.channel;

import com.shivamai.otp.otp.dto.OtpDeliveryContext;

public interface OtpDeliveryChannel {

    boolean send(
            OtpDeliveryContext context
    );

    String getChannelName();

    void ping();
}