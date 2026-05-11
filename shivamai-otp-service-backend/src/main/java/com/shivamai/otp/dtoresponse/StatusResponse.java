package com.shivamai.otp.dtoresponse;

import com.shivamai.otp.enums.ServiceStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class StatusResponse {

    private ServiceStatus status;
    private Map<String, ServiceStatus> components;
}