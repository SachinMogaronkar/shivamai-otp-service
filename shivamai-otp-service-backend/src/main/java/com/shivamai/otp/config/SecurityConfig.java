package com.shivamai.otp.config;

import com.shivamai.otp.security.ClientAccessDeniedHandler;
import com.shivamai.otp.security.ClientAuthEntryPoint;
import com.shivamai.otp.security.ClientAuthenticationFilter;
import com.shivamai.otp.security.JwtAdminFilter;
import com.shivamai.otp.security.JwtDeveloperFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final ClientAuthenticationFilter clientFilter;
    private final JwtAdminFilter adminFilter;
    private final JwtDeveloperFilter developerFilter;

    private final ClientAuthEntryPoint entryPoint;
    private final ClientAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            ClientAuthenticationFilter clientFilter,
            JwtAdminFilter adminFilter,
            JwtDeveloperFilter developerFilter,
            ClientAuthEntryPoint entryPoint,
            ClientAccessDeniedHandler accessDeniedHandler
    ) {
        this.clientFilter = clientFilter;
        this.adminFilter = adminFilter;
        this.developerFilter = developerFilter;
        this.entryPoint = entryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

                .cors(cors -> {})

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/health/**",
                                "/status/**",
                                "/otp/**",
                                "/auth/**"
                        ).permitAll()

                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/developer/**")
                        .hasRole("DEVELOPER")

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        adminFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .addFilterBefore(
                        clientFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .addFilterBefore(
                        developerFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}