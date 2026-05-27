package com.shivamai.otp.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableScheduling
public class AppConfig {
    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Shivaमै  OTP Infrastructure Service")
                        .version("1.0")
                        .description("Reusable OTP authentication platform for applications"));
    }

    @Bean
    public RestTemplate restTemplate() {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(
                5000
        );

        factory.setReadTimeout(
                5000
        );

        return new RestTemplate(
                factory
        );
    }
}