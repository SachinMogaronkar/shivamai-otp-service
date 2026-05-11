package com.shivamai.otp.security;

import com.shivamai.otp.entity.DeveloperClient;
import com.shivamai.otp.enums.ClientStatus;
import com.shivamai.otp.enums.Role;
import com.shivamai.otp.repository.DeveloperClientRepository;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtDeveloperFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final DeveloperClientRepository repository;
    private final JwtBlacklistService blacklistService;

    public JwtDeveloperFilter(JwtUtil jwtUtil,
                              DeveloperClientRepository repository,
                              JwtBlacklistService blacklistService) {

        this.jwtUtil = jwtUtil;
        this.repository = repository;
        this.blacklistService = blacklistService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        return !request.getRequestURI().startsWith("/developer");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        try {

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.sendError(401, "Missing token");
                return;
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.validate(token)) {
                response.sendError(401, "Invalid token");
                return;
            }

            if (blacklistService.isBlacklisted(token)) {
                response.sendError(401, "Token invalidated");
                return;
            }

            String role = jwtUtil.extractRole(token);

            if (!"DEVELOPER".equalsIgnoreCase(role)) {
                response.sendError(403, "Developer only");
                return;
            }

            String identifier = jwtUtil.extractIdentifier(token);

            DeveloperClient developer =
                    repository.findByIdentifier(identifier).orElse(null);

            if (developer == null
                    || developer.getStatus() != ClientStatus.ACTIVE
                    || developer.getRole() != Role.DEVELOPER) {

                response.sendError(403, "Developer access denied");
                return;
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            identifier,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_DEVELOPER"))
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);

        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}