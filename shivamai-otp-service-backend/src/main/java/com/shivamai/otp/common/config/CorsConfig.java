package com.shivamai.otp.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config =
                new CorsConfiguration();

        // =====================================
        // ALLOWED FRONTEND ORIGINS
        // =====================================

        config.setAllowedOrigins(
                List.of(
                        "http://localhost:5173",
                        "http://localhost:3000"
                )
        );

        // =====================================
        // ALLOWED METHODS
        // =====================================

        config.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        // =====================================
        // ALLOWED HEADERS
        // =====================================

        config.setAllowedHeaders(
                List.of("*")
        );

        // =====================================
        // EXPOSED HEADERS
        // =====================================

        config.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );

        // =====================================
        // ALLOW CREDENTIALS
        // =====================================

        config.setAllowCredentials(true);

        // =====================================
        // CACHE PREFLIGHT RESPONSE
        // =====================================

        config.setMaxAge(3600L);

        // =====================================
        // APPLY TO ALL ROUTES
        // =====================================

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                config
        );

        return source;
    }
}