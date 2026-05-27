package com.shivamai.otp.otp.service;

import com.shivamai.otp.otp.enums.RiskLevel;
import org.springframework.stereotype.Component;

@Component
public class OtpRiskEngine {

    public RiskLevel evaluateRisk(
            int attempts
    ) {

        if (attempts >= 3) {
            return RiskLevel.HIGH;
        }

        if (attempts == 2) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    public int getExpirySeconds(
            RiskLevel level
    ) {

        return switch (level) {

            case HIGH -> 60;

            case MEDIUM -> 120;

            case LOW -> 180;
        };
    }
}