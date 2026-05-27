package com.shivamai.otp.otp.channel;

import com.shivamai.otp.otp.dto.OtpDeliveryContext;
import com.shivamai.otp.otp.enums.OtpChannelType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@Slf4j
public class ChannelDeliveryRouter {

    private final Map<OtpChannelType, OtpDeliveryChannel> channelMap;

    public ChannelDeliveryRouter(
            EmailOtpDeliveryChannel emailChannel,
            SmsOtpDeliveryChannel smsChannel
    ) {

        Map<OtpChannelType, OtpDeliveryChannel> channels =
                new EnumMap<>(OtpChannelType.class);

        channels.put(
                OtpChannelType.EMAIL,
                emailChannel
        );

        channels.put(
                OtpChannelType.SMS,
                smsChannel
        );

        this.channelMap =
                Map.copyOf(channels);
    }

    public OtpDeliveryResult deliver(
            OtpDeliveryContext context
    ) {

        OtpChannelType channelType =
                resolveChannel(
                        context.getIdentifier()
                );

        OtpDeliveryChannel channel =
                channelMap.get(
                        channelType
                );

        if (channel == null) {

            log.error(
                    "No delivery channel configured for type={}",
                    channelType
            );

            return OtpDeliveryResult.builder()
                    .success(false)
                    .channelUsed(channelType)
                    .build();
        }

        boolean sent =
                channel.send(
                        context
                );

        if (!sent) {

            log.error(
                    "OTP delivery failed through channel={}",
                    channelType
            );

            return OtpDeliveryResult.builder()
                    .success(false)
                    .channelUsed(channelType)
                    .build();
        }

        log.info(
                "OTP delivered successfully through channel={}",
                channelType
        );

        return OtpDeliveryResult.builder()
                .success(true)
                .channelUsed(channelType)
                .build();
    }

    private OtpChannelType resolveChannel(
            String identifier
    ) {

        if (identifier == null
                || identifier.isBlank()) {

            throw new IllegalArgumentException(
                    "Identifier cannot be null or blank"
            );
        }

        if (isEmail(identifier)) {

            return OtpChannelType.EMAIL;
        }

        if (isPhone(identifier)) {

            return OtpChannelType.SMS;
        }

        throw new IllegalArgumentException(
                "Unsupported identifier format"
        );
    }

    private boolean isEmail(
            String identifier
    ) {

        return identifier.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        );
    }

    private boolean isPhone(
            String identifier
    ) {

        return identifier.matches(
                "^\\+?[1-9]\\d{9,14}$"
        );
    }

    public void ping() {

        for (OtpDeliveryChannel channel
                : channelMap.values()) {

            try {

                channel.ping();

                log.debug(
                        "Channel health check successful for={}",
                        channel.getChannelName()
                );

            } catch (Exception e) {

                log.error(
                        "Channel health check failed for={}",
                        channel.getChannelName(),
                        e
                );
            }
        }
    }
}