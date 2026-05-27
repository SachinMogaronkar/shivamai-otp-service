package com.shivamai.otp.otp.service;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class RequestMetadataService {

    public RequestMetadata extract() {

        RequestAttributes attributes =
                RequestContextHolder.getRequestAttributes();

        String clientId =
                null;

        String ip =
                "UNKNOWN";

        if (attributes != null) {

            clientId =
                    (String) attributes.getAttribute(
                            "clientId",
                            RequestAttributes.SCOPE_REQUEST
                    );

            try {

                HttpServletRequest request =
                        ((ServletRequestAttributes) attributes)
                                .getRequest();

                ip =
                        extractClientIp(
                                request
                        );

            } catch (Exception ignored) {
            }
        }

        return new RequestMetadata(
                clientId,
                ip
        );
    }

    private String extractClientIp(
            HttpServletRequest request
    ) {

        String forwarded =
                request.getHeader(
                        "X-Forwarded-For"
                );

        if (forwarded != null
                && !forwarded.isBlank()) {

            return forwarded.split(",")[0]
                    .trim();
        }

        return request.getRemoteAddr();
    }
}