package com.shivamai.otp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivamai.otp.dtoresponse.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class ClientAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    public ClientAccessDeniedHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        ApiResponse<String> apiResponse =
                new ApiResponse<>(
                        "FAILED",
                        "Access denied",
                        null,
                        LocalDateTime.now()
                );

        mapper.writeValue(response.getOutputStream(), apiResponse);
    }
}