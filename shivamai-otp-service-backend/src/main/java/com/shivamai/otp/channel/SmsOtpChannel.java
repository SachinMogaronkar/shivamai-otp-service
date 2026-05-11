package com.shivamai.otp.channel;

import com.shivamai.otp.enums.OtpType;
import org.springframework.stereotype.Component;

@Component
public class SmsOtpChannel implements OtpChannel {

    @Override
    public boolean send(String phone, String otp, OtpType type, int expirySeconds) {

        String message = switch (type) {
            case LOGIN -> "Your login OTP is: " + otp;
            case REGISTRATION -> "Your verification OTP is: " + otp;
        };

        System.out.println("SMS sent to: " + phone);
        System.out.println(message);

        return true;
    }

    private String buildMessage(String otp, OtpType type) {

        return switch (type) {
            case LOGIN -> "Your login OTP is: " + otp;
            case REGISTRATION -> "Your verification OTP is: " + otp;
        };
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }

    @Override
    public void ping() {
        // simulated always up
    }
}