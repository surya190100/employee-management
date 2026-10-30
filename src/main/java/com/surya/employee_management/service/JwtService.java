package com.surya.employee_management.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${jwt.secret:}") String jwtSecret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMillis) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("Configure a Base64-encoded JWT secret using the JWT_SECRET environment variable.");
        }
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("JWT expiration must be greater than zero.");
        }
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(Authentication authentication) {
        Instant issuedAt = Instant.now();

        return Jwts.builder()
                .subject(authentication.getName())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(expirationMillis, ChronoUnit.MILLIS)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parseToken(token).getPayload().getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException | IllegalStateException exception) {
            return false;
        }
    }

    private io.jsonwebtoken.Jws<io.jsonwebtoken.Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);
    }
}
