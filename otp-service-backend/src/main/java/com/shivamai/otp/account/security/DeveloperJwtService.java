package com.shivamai.otp.account.security;

import com.shivamai.otp.account.entity.DeveloperAccount;
import com.shivamai.otp.common.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class DeveloperJwtService {

    private final JwtUtil jwtUtil;

    public DeveloperJwtService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String generateToken(DeveloperAccount developer) {

        if (developer == null) {
            throw new IllegalArgumentException("Developer cannot be null");
        }

        if (developer.getAccountRole() == null) {
            throw new IllegalStateException("Developer role is missing");
        }

        return jwtUtil.generateToken(
                developer.getIdentifier(),
                developer.getAccountRole()
        );
    }
}