package com.shivamai.otp.dtoresponse;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeveloperProfileResponse {

    private String identifier;
    private boolean emailVerified;
    private String status;
    private LocalDateTime createdAt;
}
