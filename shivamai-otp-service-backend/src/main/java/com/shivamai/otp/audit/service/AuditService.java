package com.shivamai.otp.audit.service;

import com.shivamai.otp.audit.enums.AuditEventType;

public interface AuditService {

    void logDeveloperEvent(
            String clientId,
            String identifier,
            String endpoint,
            AuditEventType eventType,
            int status
    );

    void logAdminEvent(
            String identifier,
            String endpoint,
            AuditEventType eventType,
            int status
    );

    void logSystemEvent(
            String identifier,
            String endpoint,
            AuditEventType eventType,
            int status
    );
}
