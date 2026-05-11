package com.shivamai.otp.repository;

import com.shivamai.otp.entity.WebHookLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebHookLogRepository extends JpaRepository<WebHookLog, Long> {

    List<WebHookLog> findByStatusAndRetryCountLessThan(String status, int retryCount);
}