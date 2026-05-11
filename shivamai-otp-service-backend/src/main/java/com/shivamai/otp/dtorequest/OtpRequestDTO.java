package com.shivamai.otp.dtorequest;

import com.shivamai.otp.enums.OtpType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "OTP request payload")
public class OtpRequestDTO {

    @Schema(example = "user@example.com")
    @NotBlank
    @Email
    private String identifier;
    private OtpType type;

}