package com.shivamai.otp.repository;

import com.shivamai.otp.entity.ApiAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiAccessLogRepository extends JpaRepository<ApiAccessLog, Long> {
}