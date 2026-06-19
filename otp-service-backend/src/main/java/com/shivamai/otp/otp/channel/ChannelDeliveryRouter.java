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

        if (context.getChannelType() == null) {

            throw new IllegalArgumentException(
                    "Channel type is required"
            );
        }

        OtpDeliveryChannel channel =
                channelMap.get(
                        context.getChannelType()
                );

        if (channel == null) {

            log.error(
                    "No delivery channel configured for type={}",
                    context.getChannelType()
            );

            return OtpDeliveryResult.builder()
                    .success(false)
                    .channelUsed(
                            context.getChannelType()
                    )
                    .build();
        }

        boolean sent =
                channel.send(
                        context
                );

        if (!sent) {

            log.error(
                    "OTP delivery failed through channel={}",
                    context.getChannelType()
            );

            return OtpDeliveryResult.builder()
                    .success(false)
                    .channelUsed(
                            context.getChannelType()
                    )
                    .build();
        }

        log.info(
                "OTP delivered successfully through channel={}",
                context.getChannelType()
        );

        return OtpDeliveryResult.builder()
                .success(true)
                .channelUsed(
                        context.getChannelType()
                )
                .build();
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