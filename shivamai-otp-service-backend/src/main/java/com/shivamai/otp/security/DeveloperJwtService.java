package com.shivamai.otp.security;

import com.shivamai.otp.entity.DeveloperClient;
import org.springframework.stereotype.Service;

@Service
public class DeveloperJwtService {

    private final JwtUtil jwtUtil;

    public DeveloperJwtService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String generateToken(DeveloperClient developer) {

        if (developer == null) {
            throw new IllegalArgumentException("Developer cannot be null");
        }

        if (developer.getRole() == null) {
            throw new IllegalStateException("Developer role is missing");
        }

        return jwtUtil.generateToken(
                developer.getIdentifier(),
                developer.getRole()
        );
    }
}