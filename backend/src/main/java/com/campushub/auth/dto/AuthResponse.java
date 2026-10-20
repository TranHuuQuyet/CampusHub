package com.campushub.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn
) {
}
