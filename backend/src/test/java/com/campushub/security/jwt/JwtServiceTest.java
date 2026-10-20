package com.campushub.security.jwt;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET =
            "testSecretKeyForJwtServiceMustContainAtLeastThirtyTwoBytes";

    private final JwtService jwtService = new JwtService(SECRET, 3_600_000);

    @Test
    void shouldGenerateTokenExtractUsernameAndValidateToken() {
        String username = "student@campushub.edu";
        String token = jwtService.generateToken(username, Map.of("role", "USER"));

        assertNotNull(token);
        assertEquals(username, jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, username));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void shouldRejectTokenForAnotherUsername() {
        String token = jwtService.generateToken("student@campushub.edu", Map.of());

        assertFalse(jwtService.isTokenValid(token, "another@campushub.edu"));
    }

    @Test
    void shouldIdentifyExpiredToken() {
        JwtService expiredJwtService = new JwtService(SECRET, -1);
        String token = expiredJwtService.generateToken("student@campushub.edu", Map.of());

        assertTrue(expiredJwtService.isTokenExpired(token));
        assertFalse(expiredJwtService.isTokenValid(token, "student@campushub.edu"));
    }
}
