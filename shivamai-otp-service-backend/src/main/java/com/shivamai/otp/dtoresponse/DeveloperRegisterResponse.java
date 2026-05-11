package com.shivamai.otp.dtoresponse;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperRegisterResponse {

    private String clientId;

    private String clientSecret;

    private String message;
}
