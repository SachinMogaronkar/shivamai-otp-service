package com.shivamai.otp.common.exception;

public class ForbiddenOperationException extends RuntimeException {
  public ForbiddenOperationException(String message) {
      super(message);
  }
}
