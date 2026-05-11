package com.shivamai.otp.risk;

import org.springframework.stereotype.Component;

@Component
public class OtpRiskEngine {

    public RiskLevel evaluateRisk(int attempts) {

        if (attempts >= 3) {
            return RiskLevel.HIGH;
        }

        if (attempts == 2) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    public int getExpirySeconds(RiskLevel level) {

        switch (level) {

            case HIGH:
                return 60;

            case MEDIUM:
                return 120;

            default:
                return 180;
        }
    }
}