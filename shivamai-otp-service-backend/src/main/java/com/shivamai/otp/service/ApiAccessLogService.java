

package com.shivamai.otp.service;

import com.shivamai.otp.entity.ApiAccessLog;
import com.shivamai.otp.repository.ApiAccessLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ApiAccessLogService {

    private final ApiAccessLogRepository repository;

    public ApiAccessLogService(ApiAccessLogRepository repository) {
        this.repository = repository;
    }

    private static final String API_ACCESS = "API_ACCESS";

    public void logApiAccess(String clientId,
                             String identifier,
                             String endpoint,
                             String ip,
                             int status) {

        if (endpoint == null || endpoint.isBlank()) {
            return;
        }

        ApiAccessLog log = buildLog(
                clientId,
                identifier,
                endpoint,
                API_ACCESS,
                ip,
                status
        );

        save(log);
    }

    public void logEvent(String clientId,
                         String identifier,
                         String endpoint,
                         String event,
                         String ip,
                         int status) {

        ApiAccessLog log = buildLog(
                clientId,
                identifier,
                endpoint,
                event,
                ip,
                status
        );

        save(log);
    }

    private ApiAccessLog buildLog(String clientId,
                                  String identifier,
                                  String endpoint,
                                  String event,
                                  String ip,
                                  int status) {

        return ApiAccessLog.builder()
                .clientId(clientId)
                .identifier(identifier)
                .endpoint(endpoint)
                .event(event)
                .ipAddress(ip)
                .statusCode(status)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void save(ApiAccessLog accessLog) {
        try {
            repository.save(accessLog);
        } catch (Exception e) {
            log.error("Failed to persist API access log", e);
        }
    }
}