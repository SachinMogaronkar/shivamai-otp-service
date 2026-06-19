package com.shivamai.otp.common.exception;

public class RateLimitExceededException extends OtpException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}