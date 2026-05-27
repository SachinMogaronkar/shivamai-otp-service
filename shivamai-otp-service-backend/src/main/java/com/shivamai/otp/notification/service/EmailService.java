package com.shivamai.otp.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.shivamai.otp.notification.dto.EmailRequest;
import com.shivamai.otp.common.exception.OtpDeliveryException;

import jakarta.annotation.PostConstruct;

import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.core.env.Environment;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.stereotype.Service;

import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;

import java.net.URI;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.time.Duration;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;

    private final Environment environment;

    private final ObjectMapper mapper;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    private final HttpClient httpClient =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

    @PostConstruct
    public void validateConfiguration() {

        boolean isProd =
                environment.matchesProfiles("prod");

        if (isProd
                && (brevoApiKey == null
                || brevoApiKey.isBlank())) {

            throw new IllegalStateException(
                    "Brevo API key missing in production profile"
            );
        }

        log.info(
                "EmailService initialized successfully"
        );
    }

    public void send(
            EmailRequest request
    ) {

        validateRequest(request);

        try {

            log.info(
                    "Sending email to={}",
                    request.getTo()
            );

            String html =
                    templateEngine.process(
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

        } catch (OtpDeliveryException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Unexpected email delivery failure for={}",
                    request.getTo(),
                    e
            );

            throw new OtpDeliveryException(
                    "Email sending failed"
            );
        }
    }

    private void validateRequest(
            EmailRequest request
    ) {

        if (request == null) {

            throw new OtpDeliveryException(
                    "Email request cannot be null"
            );
        }

        if (request.getTo() == null
                || request.getTo().isBlank()) {

            throw new OtpDeliveryException(
                    "Invalid email address"
            );
        }

        if (request.getSubject() == null
                || request.getSubject().isBlank()) {

            throw new OtpDeliveryException(
                    "Email subject is required"
            );
        }

        if (request.getTemplate() == null
                || request.getTemplate().isBlank()) {

            throw new OtpDeliveryException(
                    "Email template is required"
            );
        }
    }

    private void sendUsingSmtp(
            EmailRequest request,
            String html
    ) throws Exception {

        MimeMessage message =
                mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(
                        message,
                        true
                );

        helper.setFrom(fromEmail);

        helper.setTo(
                request.getTo()
        );

        helper.setSubject(
                request.getSubject()
        );

        helper.setText(
                html,
                true
        );

        mailSender.send(message);

        log.info(
                "SMTP email sent successfully to={}",
                request.getTo()
        );
    }

    private void sendUsingBrevoApi(
            String to,
            String subject,
            String html
    ) throws IOException, InterruptedException {

        Map<String, Object> payload =
                Map.of(
                        "sender",
                        Map.of(
                                "name",
                                "Shivamai OTP",
                                "email",
                                fromEmail
                        ),
                        "to",
                        new Object[]{
                                Map.of(
                                        "email",
                                        to
                                )
                        },
                        "subject",
                        subject,
                        "htmlContent",
                        html
                );

        String body =
                mapper.writeValueAsString(
                        payload
                );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "https://api.brevo.com/v3/smtp/email"
                                )
                        )
                        .timeout(Duration.ofSeconds(15))
                        .header(
                                "accept",
                                "application/json"
                        )
                        .header(
                                "api-key",
                                brevoApiKey
                        )
                        .header(
                                "content-type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(body)
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() >= 400) {

            log.error(
                    "Brevo API failed. status={}, body={}",
                    response.statusCode(),
                    response.body()
            );

            throw new OtpDeliveryException(
                    "Brevo email sending failed"
            );
        }

        log.info(
                "Brevo email sent successfully to={}",
                to
        );
    }
}