package com.shivamai.otp.account.dto.response;

import com.shivamai.otp.account.enums.DeveloperAccountStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeveloperProfileResponse {

    private String fullName;
    private String identifier;
    private DeveloperAccountStatus status;
    private LocalDateTime createdAt;
}
