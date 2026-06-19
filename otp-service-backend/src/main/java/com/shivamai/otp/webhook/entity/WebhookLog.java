package com.shivamai.otp.webhook.entity;

import com.shivamai.otp.webhook.enums.WebhookEventType;
import com.shivamai.otp.webhook.enums.WebhookStatus;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_logs",
        indexes = {@Index(name = "idx_webhook_status",
                        columnList = "status"),
                @Index(name = "idx_webhook_created_at",
                        columnList = "createdAt"),
                @Index(name = "idx_webhook_retry_count",
                        columnList = "retryCount")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String targetUrl;

    @Column(nullable = false, length = 64)
    private String clientId;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private WebhookStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(length = 1000)
    private String failureReason;

    @Column
    private Integer responseStatusCode;

    @Column
    private LocalDateTime nextRetryAt;

    @Column
    private LocalDateTime deliveredAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private WebhookEventType eventType;

    @Column(length = 128)
    private String webhookSecret;

    @Column(nullable = false)
    private boolean retryInProgress;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();

        if (this.retryCount < 0) {

            this.retryCount = 0;
        }
    }
}