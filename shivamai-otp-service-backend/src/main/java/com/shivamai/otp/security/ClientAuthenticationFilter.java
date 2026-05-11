package com.shivamai.otp.security;

import com.shivamai.otp.entity.DeveloperApp;
import com.shivamai.otp.enums.AppStatus;
import com.shivamai.otp.exception.ClientAuthenticationException;
import com.shivamai.otp.repository.DeveloperAppRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ClientAuthenticationFilter extends OncePerRequestFilter {

    private final DeveloperAppRepository appRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientAuthenticationFilter(DeveloperAppRepository appRepository,
                                      PasswordEncoder passwordEncoder) {
        this.appRepository = appRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        return !path.startsWith("/otp");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String clientId = request.getHeader("X-Client-Id");
        String clientSecret = request.getHeader("X-Client-Secret");

        // 🔴 1. Missing headers
        if (clientId == null || clientSecret == null) {
            sendError(response, "Missing client credentials");
            return;
        }

        // 🔴 2. Fetch app
        DeveloperApp app = appRepository.findByClientId(clientId)
                .orElse(null);

        // 🔴 3. Validate credentials (single unified check)
        if (app == null || !passwordEncoder.matches(clientSecret, app.getClientSecret())) {
            sendError(response, "Invalid client credentials");
            return;
        }

        // 🔴 4. Check app status
        if (app.getStatus() != AppStatus.ACTIVE) {
            sendError(response, "Application suspended");
            return;
        }

        // ✅ 5. Attach context (for usage tracking)
        request.setAttribute("clientId", app.getClientId());
        request.setAttribute("appId", app.getId());

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        response.getWriter().write("""
        {
            "status": "FAILED",
            "message": "%s"
        }
        """.formatted(message));
    }
}