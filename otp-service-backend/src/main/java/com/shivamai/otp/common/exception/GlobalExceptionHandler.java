package com.shivamai.otp.common.exception;

import com.shivamai.otp.common.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =====================================
    // CLIENT AUTHENTICATION
    // =====================================

    @ExceptionHandler(ApplicationAuthenticationException.class)
    public ResponseEntity<ApiResponse<String>>
    handleClientAuthException(
            ApplicationAuthenticationException ex
    ) {

        log.warn(
                "Client authentication failed: {}",
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
    }

    // =====================================
    // UNAUTHORIZED
    // =====================================

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<String>>
    handleUnauthorized(
            UnauthorizedException ex
    ) {

        log.warn(
                "Unauthorized access: {}",
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
    }

    // =====================================
    // FORBIDDEN
    // =====================================

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ApiResponse<String>>
    handleForbidden(
            ForbiddenOperationException ex
    ) {

        log.warn(
                "Forbidden operation: {}",
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
    }

    // =====================================
    // RESOURCE NOT FOUND
    // =====================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>>
    handleNotFound(
            ResourceNotFoundException ex
    ) {

        log.warn(
                "Resource not found: {}",
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    // =====================================
    // RATE LIMIT
    // =====================================

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<String>>
    handleRateLimit(
            RateLimitExceededException ex
    ) {

        log.warn(
                "Rate limit exceeded: {}",
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                ex.getMessage()
        );
    }

    // =====================================
    // DELIVERY FAILURES
    // =====================================

    @ExceptionHandler(OtpDeliveryException.class)
    public ResponseEntity<ApiResponse<String>>
    handleDelivery(
            OtpDeliveryException ex
    ) {

        log.error(
                "OTP delivery failed: {}",
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage()
        );
    }

    // =====================================
    // OTP BUSINESS EXCEPTIONS
    // =====================================

    @ExceptionHandler(OtpException.class)
    public ResponseEntity<ApiResponse<String>>
    handleOtp(
            OtpException ex
    ) {

        log.warn(
                "OTP exception occurred: {}",
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    // =====================================
    // INVALID BUSINESS REQUESTS
    // =====================================

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<String>>
    handleInvalidRequest(
            InvalidRequestException ex
    ) {

        log.warn(
                "Invalid request: {}",
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    // =====================================
    // VALIDATION FAILURES
    // =====================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>>
    handleValidationException(
            MethodArgumentNotValidException ex
    ) {

        String message =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .findFirst()
                        .map(error ->
                                error.getDefaultMessage()
                        )
                        .orElse("Validation failed");

        log.warn(
                "Validation failed: {}",
                message
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    // =====================================
    // GENERIC FAILURES
    // =====================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>>
    handleGeneral(
            Exception ex
    ) {

        log.error(
                "Unexpected internal server error",
                ex
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error"
        );
    }

    // =====================================
    // CENTRAL RESPONSE BUILDER
    // =====================================

    private ResponseEntity<ApiResponse<String>>
    buildResponse(
            HttpStatus status,
            String message
    ) {

        ApiResponse<String> response =
                new ApiResponse<>(
                        "FAILED",
                        message,
                        null,
                        LocalDateTime.now()
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // =====================================
    // RESOURCE CONFLICT
    // =====================================

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiResponse<String>>
    handleConflict(
            ResourceConflictException ex
    ) {

        log.warn(
                "Resource conflict: {}",
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }
}