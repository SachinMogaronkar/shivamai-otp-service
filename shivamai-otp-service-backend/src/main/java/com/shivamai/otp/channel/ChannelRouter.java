package com.shivamai.otp.channel;

import com.shivamai.otp.enums.OtpChannelType;
import com.shivamai.otp.enums.OtpType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChannelRouter {

    private final Map<OtpChannelType, OtpChannel> channelMap = new HashMap<>();

    @Autowired
    public ChannelRouter(EmailOtpChannel emailChannel,
                         SmsOtpChannel smsChannel) {

        channelMap.put(OtpChannelType.EMAIL, emailChannel);
        channelMap.put(OtpChannelType.SMS, smsChannel);
    }

    public DeliveryResult deliver(String identifier, String otp, OtpType type, int expirySeconds) {

        OtpChannelType channelType = resolveChannel(identifier);

        if (channelType == null) {
            return new DeliveryResult(false, null);
        }

        OtpChannel channel = channelMap.get(channelType);

        if (channel == null) {
            return new DeliveryResult(false, null);
        }

        boolean sent = channel.send(identifier, otp, type, expirySeconds);

        return new DeliveryResult(sent, channelType);
    }

    private OtpChannelType resolveChannel(String identifier) {

        if (isEmail(identifier)) return OtpChannelType.EMAIL;

        if (isPhone(identifier)) return OtpChannelType.SMS;

        return null;
    }

    private boolean isEmail(String identifier) {
        return identifier.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isPhone(String identifier) {
        return identifier.matches("^\\+?[1-9]\\d{9,14}$");
    }

    public void ping() {

        for (OtpChannel channel : channelMap.values()) {
            channel.ping();
        }
    }
}