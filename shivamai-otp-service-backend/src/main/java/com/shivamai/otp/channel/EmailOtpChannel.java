package com.shivamai.otp.channel;

import com.shivamai.otp.dtorequest.EmailRequest;
import com.shivamai.otp.enums.OtpType;
import com.shivamai.otp.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class EmailOtpChannel implements OtpChannel {

    private final EmailService emailService;

    @Override
    public boolean send(String email, String otp, OtpType type, int expirySeconds) {

        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("expiryMinutes", expirySeconds/60);

        String template = resolveTemplate(type);
        String subject = resolveSubject(type);

        EmailRequest request = EmailRequest.builder()
                .to(email)
                .subject(subject)
                .template(template)
                .context(context)
                .build();

        emailService.send(request);

        return true;
    }

    private String resolveTemplate(OtpType type) {

        return switch (type) {
            case LOGIN -> "email/login-otp";
            case REGISTRATION -> "email/otp-email";
        };
    }

    private String resolveSubject(OtpType type) {

        return switch (type) {
            case LOGIN -> "Developer Login OTP";
            case REGISTRATION -> "OTP Verification";
        };
    }

    @Override
    public String getChannelName() {
        return "EMAIL";
    }

    @Override
    public void ping() {

        try {
            // lightweight check (no email sent)
            if (emailService == null) {
                throw new RuntimeException("Email service unavailable");
            }
        } catch (Exception e) {
            throw new RuntimeException("Email channel down");
        }
    }
}