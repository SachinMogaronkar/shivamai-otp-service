package com.shivamai.otp.audit.dto.request;

import com.shivamai.otp.audit.enums.AuditActorType;
import com.shivamai.otp.audit.enums.AuditEventType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuditLogRequest {

    private String clientId;

    private String identifier;

    private AuditActorType actorType;

    private String endpoint;

    private AuditEventType eventType;

    private String ip;

    private int status;
}