package com.surya.employee_management.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    public static AuthResponse of(String accessToken, long expiresInSeconds) {
        return new AuthResponse(accessToken, TOKEN_TYPE_BEARER, expiresInSeconds);
    }
}

