package com.shivamai.otp.common.security;

import com.shivamai.otp.account.enums.AccountRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;

import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import java.security.Key;

import java.util.Date;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    private final Key key;

    private final long expiration;

    public JwtUtil(
            @Value("${shivamai.jwt.secret}") String secret,
            @Value("${shivamai.jwt.expiration}") long expiration
    ) {

        // =====================================
        // SECRET VALIDATION
        // =====================================

        if (secret == null || secret.length() < 32) {

            throw new IllegalStateException(
                    "JWT secret must be at least 32 characters long"
            );
        }

        this.key =
                Keys.hmacShaKeyFor(
                        secret.getBytes(StandardCharsets.UTF_8)
                );

        this.expiration = expiration;
    }

    // =====================================
    // TOKEN GENERATION
    // =====================================

    public String generateToken(
            String identifier,
            AccountRole accountRole
    ) {

        return Jwts.builder()
                .setSubject(identifier)
                .claim("role", accountRole.name())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expiration
                        )
                )
                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    // =====================================
    // CLAIM EXTRACTION
    // =====================================

    public String extractIdentifier(
            String token
    ) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    public String extractRole(
            String token
    ) {

        return extractClaim(
                token,
                claims -> claims.get(
                        "role",
                        String.class
                )
        );
    }

    public Date extractExpiration(
            String token
    ) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    // =====================================
    // GENERIC CLAIM EXTRACTION
    // =====================================

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        Claims claims =
                extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    // =====================================
    // TOKEN VALIDATION
    // =====================================

    public boolean validate(
            String token
    ) {

        try {

            extractAllClaims(token);

            return true;

        } catch (ExpiredJwtException e) {

            log.warn(
                    "JWT expired"
            );

        } catch (UnsupportedJwtException e) {

            log.warn(
                    "Unsupported JWT"
            );

        } catch (MalformedJwtException e) {

            log.warn(
                    "Malformed JWT"
            );

        } catch (IllegalArgumentException e) {

            log.warn(
                    "JWT claims string is empty"
            );

        } catch (JwtException e) {

            log.warn(
                    "JWT validation failed"
            );
        }

        return false;
    }

    // =====================================
    // INTERNAL CLAIM PARSER
    // =====================================

    private Claims extractAllClaims(
            String token
    ) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}