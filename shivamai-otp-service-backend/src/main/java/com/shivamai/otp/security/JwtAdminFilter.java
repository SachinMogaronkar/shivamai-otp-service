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
public class JwtAdminFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final DeveloperClientRepository repository;
    private final JwtBlacklistService blacklistService;

    public JwtAdminFilter(JwtUtil jwtUtil,
                          DeveloperClientRepository repository,
                          JwtBlacklistService blacklistService) {
        this.jwtUtil = jwtUtil;
        this.repository = repository;
        this.blacklistService = blacklistService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        return !request.getRequestURI().startsWith("/admin");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        try {

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.sendError(401, "Missing JWT");
                return;
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.validate(token)) {
                response.sendError(401, "Invalid JWT");
                return;
            }

            if (blacklistService.isBlacklisted(token)) {
                response.sendError(401, "Token invalidated");
                return;
            }

            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equalsIgnoreCase(role)) {
                response.sendError(403, "Admin only");
                return;
            }

            String identifier = jwtUtil.extractIdentifier(token);

            DeveloperClient admin =
                    repository.findByIdentifier(identifier).orElse(null);

            if (admin == null
                    || admin.getRole() != Role.ADMIN
                    || admin.getStatus() != ClientStatus.ACTIVE) {

                response.sendError(403, "Admin access denied");
                return;
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            identifier,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);

        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}