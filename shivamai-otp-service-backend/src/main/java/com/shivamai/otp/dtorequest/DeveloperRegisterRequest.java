package com.shivamai.otp.dtorequest;

import com.shivamai.otp.enums.OtpChannelType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class DeveloperRegisterRequest {

    private String applicationName;

    @Email
    @NotBlank
    private String identifier;

    private String password;

    private Set<OtpChannelType> allowedChannels;

}