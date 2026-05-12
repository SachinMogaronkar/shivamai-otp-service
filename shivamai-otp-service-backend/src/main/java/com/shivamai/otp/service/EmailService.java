package com.shivamai.otp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivamai.otp.dtorequest.EmailRequest;
import com.shivamai.otp.exception.OtpDeliveryException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final Environment environment;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    private final ClassPathResource logo =
            new ClassPathResource("static/images/brand1.png");

    private final ObjectMapper mapper =
            new ObjectMapper();

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

            boolean isProd =
                    environment.matchesProfiles("prod");

            if (isProd) {

                sendUsingBrevoApi(
                        request.getTo(),
                        request.getSubject(),
                        html
                );

            } else {

                sendUsingSmtp(
                        request,
                        html
                );
            }

        } catch (Exception e) {

            log.error("Failed to send email to {}", request.getTo(), e);

            throw new OtpDeliveryException("Email sending failed");
        }
    }

    private void sendUsingSmtp(
            EmailRequest request,
            String html
    ) throws Exception {

        MimeMessage message =
                mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        helper.setText(html, true);

        helper.addInline("logoImage", logo);

        mailSender.send(message);
    }

    private void sendUsingBrevoApi(
            String to,
            String subject,
            String html
    ) throws IOException, InterruptedException {

        HttpClient client =
                HttpClient.newHttpClient();

        Map<String, Object> payload = Map.of(
                "sender", Map.of(
                        "name", "Shivamai OTP",
                        "email", fromEmail
                ),
                "to", new Object[]{
                        Map.of("email", to)
                },
                "subject", subject,
                "htmlContent", html
        );

        String body =
                mapper.writeValueAsString(payload);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                        .header("accept", "application/json")
                        .header("api-key", brevoApiKey)
                        .header("content-type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() >= 400) {

            log.error(
                    "Brevo API failed: {}",
                    response.body()
            );

            throw new OtpDeliveryException(
                    "Brevo email sending failed"
            );
        }

        log.info("Brevo email sent successfully");
    }
}