package com.shivamai.otp.service.service_implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivamai.otp.entity.OtpRequest;
import com.shivamai.otp.entity.WebHookLog;
import com.shivamai.otp.repository.WebHookLogRepository;
import com.shivamai.otp.service.WebHookService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebHookServiceImpl implements WebHookService {

    private final WebHookLogRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 3;

    @Override
    public void sendVerificationEvent(String url, OtpRequest request) {

        try {

            Map<String, Object> payload = Map.of(
                    "identifier", request.getIdentifier(),
                    "requestId", request.getId(),
                    "status", request.getStatus().name(),
                    "verifiedAt", request.getVerifiedAt(),
                    "otpType", request.getOtpType().name(),
                    "channel", request.getChannel()
            );

            String jsonPayload = objectMapper.writeValueAsString(payload);

            WebHookLog webhookLog = WebHookLog.builder()
                    .url(url)
                    .payload(jsonPayload)
                    .status("PENDING")
                    .retryCount(0)
                    .createdAt(LocalDateTime.now())
                    .build();

            repository.save(webhookLog);

            send(webhookLog);

        } catch (Exception e) {
            log.error("Webhook processing failed for url: {}", url, e);
        }
    }

    // 🔹 Core send logic
    private void send(WebHookLog webhookLog) {

        try {

            // ✅ Proper JSON headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(
                    webhookLog.getPayload(),
                    headers
            );

            // ✅ Correct assignment
            ResponseEntity<String> response = restTemplate.postForEntity(
                    webhookLog.getUrl(),
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {

                webhookLog.setStatus("SUCCESS");
                log.info("Webhook delivered successfully to {}", webhookLog.getUrl());

            } else {

                throw new RuntimeException(
                        "Non-success response: " + response.getStatusCode()
                );
            }

        } catch (Exception e) {

            webhookLog.setStatus("FAILED");
            webhookLog.setRetryCount(webhookLog.getRetryCount() + 1);

            log.warn("Webhook delivery failed for {}, retryCount={}",
                    webhookLog.getUrl(),
                    webhookLog.getRetryCount());
        }

        repository.save(webhookLog);
    }

    // 🔁 Retry failed webhooks every 60 sec
    @Scheduled(fixedDelay = 60000)
    public void retryFailedWebhooks() {

        List<WebHookLog> failedLogs =
                repository.findByStatusAndRetryCountLessThan("FAILED", MAX_RETRIES);

        for (WebHookLog webhookLog : failedLogs) {
            send(webhookLog);
        }
    }
}