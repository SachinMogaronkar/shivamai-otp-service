package com.shivamai.otp.audit.logging;

import com.shivamai.otp.audit.dto.request.AuditLogRequest;
import com.shivamai.otp.audit.entity.AuditLog;

import com.shivamai.otp.audit.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiAccessLogger {

    private final AuditLogRepository repository;

    // =====================================
    // MAIN AUDIT LOGGER
    // =====================================

    public void logEvent(
            AuditLogRequest request
    ) {

        if (request.getEndpoint() == null
                || request.getEndpoint().isBlank()) {

            log.warn(
                    "Skipping audit log due to blank endpoint"
            );

            return;
        }

        try {

            AuditLog accessLog =
                    buildLog(request);

            repository.save(accessLog);

        } catch (Exception e) {

            log.error(
                    """
                    Failed to persist audit log:
                    clientId={},
                    identifier={},
                    actorType={},
                    endpoint={},
                    event={}
                    """,
                    request.getClientId(),
                    request.getIdentifier(),
                    request.getActorType(),
                    request.getEndpoint(),
                    request.getEventType(),
                    e
            );
        }
    }

    // =====================================
    // INTERNAL BUILDER
    // =====================================

    private AuditLog buildLog(
            AuditLogRequest request
    ) {

        return AuditLog.builder()
                .clientId(
                        request.getClientId()
                )
                .identifier(
                        request.getIdentifier()
                )
                .actorType(
                        request.getActorType()
                )
                .endpoint(
                        request.getEndpoint()
                )
                .eventType(
                        request.getEventType()
                )
                .ipAddress(
                        request.getIp()
                )
                .statusCode(
                        request.getStatus()
                )
                .createdAt(
                        LocalDateTime.now()
                )
                .build();
    }
}