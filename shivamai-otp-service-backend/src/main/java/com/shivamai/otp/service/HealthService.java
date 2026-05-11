package com.shivamai.otp.service;

import com.shivamai.otp.channel.ChannelRouter;
import com.shivamai.otp.dtoresponse.StatusResponse;
import com.shivamai.otp.enums.ServiceStatus;

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
    private final ChannelRouter channelRouter;

    public StatusResponse getPublicStatus() {

        Map<String, ServiceStatus> components = new HashMap<>();

        components.put("otp_request", checkOtpRequest());
        components.put("otp_verification", checkOtpVerification());
        components.put("otp_delivery", checkDelivery());

        ServiceStatus overall = calculateOverall(components);

        return StatusResponse.builder()
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
            channelRouter.ping();
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