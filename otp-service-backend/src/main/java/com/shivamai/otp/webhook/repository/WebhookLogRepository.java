package com.shivamai.otp.webhook.repository;

import com.shivamai.otp.webhook.entity.WebhookLog;
import com.shivamai.otp.webhook.enums.WebhookStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

public interface WebhookLogRepository
        extends JpaRepository<WebhookLog, Long>,
        JpaSpecificationExecutor<WebhookLog> {

    List<WebhookLog>
    findByStatusAndRetryInProgressFalseAndRetryCountLessThanAndNextRetryAtBefore(
            WebhookStatus status,
            int retryCount,
            LocalDateTime now
    );

    Optional<WebhookLog>
    findByIdAndClientId(
            Long id,
            String clientId
    );
}