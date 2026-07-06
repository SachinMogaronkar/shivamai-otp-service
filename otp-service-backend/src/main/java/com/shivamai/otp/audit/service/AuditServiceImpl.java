package com.shivamai.otp.audit.service;

import com.shivamai.otp.audit.dto.request.AuditLogRequest;
import com.shivamai.otp.audit.enums.AuditActorType;
import com.shivamai.otp.audit.enums.AuditEventType;
import com.shivamai.otp.audit.logging.ApiAccessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl
        implements AuditService {

    private final ApiAccessLogger apiAccessLogger;

    @Override
    public void logAccountEvent(
            String clientId,
            String identifier,
            String endpoint,
            AuditEventType eventType,
            int status
    ) {

        log(
                clientId,
                identifier,
                AuditActorType.DEVELOPER,
                endpoint,
                eventType,
                status
        );
    }

    @Override
    public void logAdminEvent(
            String identifier,
            String endpoint,
            AuditEventType eventType,
            int status
    ) {

        log(
                "system-admin",
                identifier,
                AuditActorType.ADMIN,
                endpoint,
                eventType,
                status
        );
    }

    @Override
    public void logSystemEvent(
            String identifier,
            String endpoint,
            AuditEventType eventType,
            int status
    ) {

        log(
                "SYSTEM",
                identifier,
                AuditActorType.SYSTEM,
                endpoint,
                eventType,
                status
        );
    }

    private void log(
            String clientId,
            String identifier,
            AuditActorType actorType,
            String endpoint,
            AuditEventType eventType,
            int status
    ) {

        apiAccessLogger.logEvent(
                AuditLogRequest.builder()
                        .clientId(clientId)
                        .identifier(identifier)
                        .actorType(actorType)
                        .endpoint(endpoint)
                        .eventType(eventType)
                        .ip("UNKNOWN")
                        .status(status)
                        .build()
        );
    }
}
