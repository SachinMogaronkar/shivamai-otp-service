package com.shivamai.otp.service;

import com.shivamai.otp.entity.ApiAccessLog;
import com.shivamai.otp.repository.ApiAccessLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailTestService {

    private final JavaMailSender mailSender;

    public void sendTestMail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        try {

            log.info("Sending test email to {}", email);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Shivamai SMTP Test");
            message.setText("SMTP configuration working successfully.");

            mailSender.send(message);

        } catch (Exception e) {

            log.error("Failed to send test email to {}", email, e);

            throw new RuntimeException("Test mail failed");
        }
    }
}