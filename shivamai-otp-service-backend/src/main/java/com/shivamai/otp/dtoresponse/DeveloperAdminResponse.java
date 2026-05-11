package com.shivamai.otp.dtoresponse;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeveloperAdminResponse {

    private Long id;
    private String identifier;
    private String status;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}
