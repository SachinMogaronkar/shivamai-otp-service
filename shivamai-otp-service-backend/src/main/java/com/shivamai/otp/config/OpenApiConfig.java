package com.shivamai.otp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI otpServiceOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Shivaमै OTP Authentication Service")
                        .description("Secure OTP generation and verification service")
                        .version("1.0"));
    }
}