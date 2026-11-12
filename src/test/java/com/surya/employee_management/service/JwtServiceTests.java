package com.surya.employee_management.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTests {

    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));
    private final JwtService jwtService = new JwtService(TEST_SECRET, 3_600_000);

    @Test
    void generatesSignedTokenWithExpectedSubjectAndLifetime() {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "test-user", null, List.of());

        String token = jwtService.generateToken(authentication);

        assertTrue(jwtService.isTokenValid(token));
        assertEquals("test-user", jwtService.extractUsername(token));
        assertEquals(3600, jwtService.getExpirationSeconds());
    }

    @Test
    void rejectsTamperedAndExpiredTokens() {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "test-user", null, List.of());
        String[] parts = jwtService.generateToken(authentication).split("\\.");
        char[] signature = parts[2].toCharArray();
        signature[0] = signature[0] == 'A' ? 'B' : 'A';
        String tamperedToken = parts[0] + "." + parts[1] + "." + new String(signature);

        Instant now = Instant.now();
        String expiredToken = Jwts.builder()
                .subject("test-user")
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET)), Jwts.SIG.HS256)
                .compact();

        assertFalse(jwtService.isTokenValid(tamperedToken));
        assertFalse(jwtService.isTokenValid(expiredToken));
    }
}
