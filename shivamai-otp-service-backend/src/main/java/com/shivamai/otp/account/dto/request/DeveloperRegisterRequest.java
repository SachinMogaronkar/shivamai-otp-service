package com.shivamai.otp.account.dto.request;

import com.shivamai.otp.otp.enums.OtpChannelType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class DeveloperRegisterRequest {

    private String applicationName;

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @Email
    @NotBlank
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    private Set<OtpChannelType> allowedChannels;

}