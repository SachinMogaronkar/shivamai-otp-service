package com.shivamai.otp.health.service;

import com.shivamai.otp.otp.service.OtpService;
import com.shivamai.otp.otp.service.OtpVerificationService;
import com.shivamai.otp.otp.channel.ChannelDeliveryRouter;
import com.shivamai.otp.health.dto.SystemStatusResponse;
import com.shivamai.otp.health.enums.ServiceStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthService {

    private final OtpService otpService;
    private final OtpVerificationService verificationService;
    private final ChannelDeliveryRouter channelDeliveryRouter;

    public SystemStatusResponse getPublicStatus() {

        Map<String, ServiceStatus> components = new HashMap<>();

        components.put("otp_request", checkOtpRequest());
        components.put("otp_verification", checkOtpVerification());
        components.put("otp_delivery", checkDelivery());

        ServiceStatus overall = calculateOverall(components);

        return SystemStatusResponse.builder()
                .status(overall)
                .components(components)
                .build();
    }

    private ServiceStatus checkOtpRequest() {
        try {
            otpService.pingRequest();
            return ServiceStatus.OPERATIONAL;
        } catch (Exception e) {
            log.warn("OTP request service DOWN", e);
            return ServiceStatus.DOWN;
        }
    }

    private ServiceStatus checkOtpVerification() {
        try {
            verificationService.ping();
            return ServiceStatus.OPERATIONAL;
        } catch (Exception e) {
            log.warn("OTP verification service DOWN", e);
            return ServiceStatus.DOWN;
        }
    }

    private ServiceStatus checkDelivery() {
        try {
            channelDeliveryRouter.ping();
            return ServiceStatus.OPERATIONAL;
        } catch (Exception e) {
            log.warn("OTP delivery service DOWN", e);
            return ServiceStatus.DOWN;
        }
    }

    private ServiceStatus calculateOverall(Map<String, ServiceStatus> components) {

        if (components.containsValue(ServiceStatus.DOWN)) {
            return ServiceStatus.DOWN;
        }

        if (components.containsValue(ServiceStatus.DEGRADED)) {
            return ServiceStatus.DEGRADED;
        }

        return ServiceStatus.OPERATIONAL;
    }
}