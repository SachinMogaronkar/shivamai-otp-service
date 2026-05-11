package com.shivamai.otp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivamai.otp.dtoresponse.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class ClientAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    public ClientAuthEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ApiResponse<String> apiResponse =
                new ApiResponse<>(
                        "FAILED",
                        "Unauthorized access",
                        null,
                        LocalDateTime.now()
                );

        mapper.writeValue(response.getOutputStream(), apiResponse);
    }
}