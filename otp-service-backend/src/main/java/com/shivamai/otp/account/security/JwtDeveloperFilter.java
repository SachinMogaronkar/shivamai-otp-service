package com.shivamai.otp.account.security;

import com.shivamai.otp.account.entity.DeveloperAccount;
import com.shivamai.otp.account.enums.DeveloperAccountStatus;
import com.shivamai.otp.account.enums.AccountRole;
import com.shivamai.otp.account.repository.DeveloperAccountRepository;

import com.shivamai.otp.common.security.JwtBlacklistService;
import com.shivamai.otp.common.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtDeveloperFilter
        extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final DeveloperAccountRepository repository;

    private final JwtBlacklistService blacklistService;

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        if ("OPTIONS".equalsIgnoreCase(
                request.getMethod()
        )) {

            return true;
        }

        return !request.getRequestURI()
                .startsWith("/developer");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing token"
            );

            return;
        }

        String token =
                authHeader.substring(7);

        if (!jwtUtil.validate(token)) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid token"
            );

            return;
        }

        if (blacklistService.isBlacklisted(token)) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token invalidated"
            );

            return;
        }

        String role =
                jwtUtil.extractRole(token);

        if (!AccountRole.DEVELOPER.name().equalsIgnoreCase(role)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Developer only"
            );

            return;
        }

        String identifier =
                jwtUtil.extractIdentifier(token);

        DeveloperAccount developer =
                repository.findByIdentifier(identifier)
                        .orElse(null);

        if (developer == null) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Developer not found"
            );

            return;
        }

        if (developer.getAccountRole()
                != AccountRole.DEVELOPER) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Developer only"
            );

            return;
        }

        if (SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            identifier,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_DEVELOPER"
                                    )
                            )
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(auth);
        }

        log.debug(
                "Developer authenticated: {}",
                identifier
        );

        chain.doFilter(
                request,
                response
        );
    }
}