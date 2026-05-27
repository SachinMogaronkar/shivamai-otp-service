package com.shivamai.otp.otp.channel;

import com.shivamai.otp.notification.dto.EmailRequest;
import com.shivamai.otp.notification.service.EmailService;

import com.shivamai.otp.otp.dto.OtpDeliveryContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailOtpDeliveryChannel
        implements OtpDeliveryChannel {

    private final EmailService emailService;

    @Override
    public boolean send(
            OtpDeliveryContext deliveryContext
    ) {

        try {

            EmailRequest request =
                    EmailRequest.builder()
                            .to(
                                    deliveryContext.getIdentifier()
                            )
                            .subject(
                                    deliveryContext.getSubject()
                            )
                            .template(
                                    deliveryContext.getTemplate()
                            )
                            .context(
                                    deliveryContext.getContext()
                            )
                            .build();

            emailService.send(
                    request
            );

            log.info(
                    "OTP email delivered successfully to={}",
                    deliveryContext.getIdentifier()
            );

            return true;

        } catch (Exception e) {

            log.error(
                    "Failed to deliver OTP email",
                    e
            );

            return false;
        }
    }

    @Override
    public String getChannelName() {

        return "EMAIL";
    }

    @Override
    public void ping() {

        if (emailService == null) {

            throw new RuntimeException(
                    "Email channel unavailable"
            );
        }
    }
}