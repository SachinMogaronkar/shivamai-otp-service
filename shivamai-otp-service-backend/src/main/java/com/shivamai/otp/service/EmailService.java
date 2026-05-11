package com.shivamai.otp.service;

import com.shivamai.otp.dtorequest.EmailRequest;
import com.shivamai.otp.exception.OtpDeliveryException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    private final ClassPathResource logo =
            new ClassPathResource("static/images/brand1.png");

    public void send(EmailRequest request) {

        if (request.getTo() == null || request.getTo().isBlank()) {
            throw new OtpDeliveryException("Invalid email address");
        }

        try {

            log.info("Sending email to {}", request.getTo());

            String html = templateEngine.process(
                    request.getTemplate(),
                    request.getContext()
            );

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(html, true);

            helper.addInline("logoImage", logo);

            mailSender.send(message);

        } catch (Exception e) {

            log.error("Failed to send email to {}", request.getTo(), e);

            throw new OtpDeliveryException("Email sending failed");
        }
    }
}