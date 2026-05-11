package com.shivamai.otp.cache;

public class CachedOtp {

    private Long requestId;
    private String otp;
    private String otpHash;
    private long expiryTime;

    public CachedOtp(Long requestId, String otp, String otpHash, long expiryTime) {
        this.requestId = requestId;
        this.otp = otp;
        this.otpHash = otpHash;
        this.expiryTime = expiryTime;
    }

    public String getOtp() {
        return otp;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public long getExpiryTime() {
        return expiryTime;
    }
}