package com.shivamai.otp.common.config;

import com.shivamai.otp.common.security.ApplicationAccessDeniedHandler;
import com.shivamai.otp.common.security.ApplicationAuthEntryPoint;
import com.shivamai.otp.common.security.ApplicationAuthenticationFilter;
import com.shivamai.otp.account.security.JwtAdminFilter;
import com.shivamai.otp.account.security.JwtDeveloperFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApplicationAuthenticationFilter clientFilter;

    private final JwtAdminFilter adminFilter;

    private final JwtDeveloperFilter developerFilter;

    private final ApplicationAuthEntryPoint entryPoint;

    private final ApplicationAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // =====================================
                // CORS
                // =====================================

                .cors(cors -> {})

                // =====================================
                // DISABLE CSRF
                // =====================================

                .csrf(csrf -> csrf.disable())

                // =====================================
                // DISABLE FORM LOGIN
                // =====================================

                .formLogin(form -> form.disable())

                // =====================================
                // DISABLE HTTP BASIC AUTH
                // =====================================

                .httpBasic(basic -> basic.disable())

                // =====================================
                // STATELESS SESSION POLICY
                // =====================================

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // =====================================
                // SECURITY EXCEPTION HANDLING
                // =====================================

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // =====================================
                // ROUTE AUTHORIZATION
                // =====================================

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/health/**",
                                "/status/**",
                                "/otp/**",
                                "/auth/**"
                        )
                        .permitAll()

                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/developer/**")
                        .hasRole("DEVELOPER")

                        .anyRequest()
                        .authenticated()
                )

                // =====================================
                // FILTER CHAIN
                // =====================================

                .addFilterBefore(
                        clientFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .addFilterBefore(
                        adminFilter,
                        ApplicationAuthenticationFilter.class
                )

                .addFilterBefore(
                        developerFilter,
                        JwtAdminFilter.class
                );

        return http.build();
    }
}