package com.shivamai.otp.exception;

public class RateLimitExceededException extends OtpException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}