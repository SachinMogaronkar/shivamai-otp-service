package com.shivamai.otp.audit.entity;

import com.shivamai.otp.audit.enums.AuditActorType;
import com.shivamai.otp.audit.enums.AuditEventType;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs",
        indexes = {@Index(name = "idx_audit_client_id",
                        columnList = "clientId"),
                @Index(name = "idx_audit_identifier",
                        columnList = "identifier"),
                @Index(name = "idx_audit_actor_type",
                        columnList = "actorType"),
                @Index(name = "idx_audit_event_type",
                        columnList = "eventType"),
                @Index(name = "idx_audit_timestamp",
                        columnList = "createdAt")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String clientId;

    @Column(length = 255)
    private String identifier;

    @Column(
            nullable = false,
            length = 255
    )
    private String endpoint;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private AuditActorType actorType;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 60
    )
    private AuditEventType eventType;

    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private int statusCode;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();
    }
}