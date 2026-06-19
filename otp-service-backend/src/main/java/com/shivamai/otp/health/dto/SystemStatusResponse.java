package com.shivamai.otp.health.dto;

import com.shivamai.otp.health.enums.ServiceStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class SystemStatusResponse {

    private ServiceStatus status;
    private Map<String, ServiceStatus> components;
}