package com.shivamai.otp.exception;

import com.shivamai.otp.dtoresponse.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientAuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleClientAuthException(
            ClientAuthenticationException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "FAILED",
                        ex.getMessage(),
                        null,
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<String>> handleRateLimit(
            RateLimitExceededException ex) {

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ApiResponse<>(
                        "FAILED",
                        ex.getMessage(),
                        null,
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(OtpDeliveryException.class)
    public ResponseEntity<ApiResponse<String>> handleDelivery(
            OtpDeliveryException ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                        "FAILED",
                        ex.getMessage(),
                        null,
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(OtpException.class)
    public ResponseEntity<ApiResponse<String>> handleOtp(
            OtpException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        "FAILED",
                        ex.getMessage(),
                        null,
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneral(Exception ex) {

        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                        "FAILED",
                        ex.getMessage(),   // 👈 IMPORTANT
                        null,
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");

        return ResponseEntity.badRequest().body(
                new ApiResponse<>(
                        "FAILED",
                        message,
                        null,
                        LocalDateTime.now()
                )
        );
    }
}