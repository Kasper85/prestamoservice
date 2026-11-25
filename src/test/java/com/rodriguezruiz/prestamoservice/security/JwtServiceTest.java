package com.rodriguezruiz.prestamoservice.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET_BASE64 =
            "cHJlc3RhY29udHJvbC10ZXN0LXNlY3JldC1rZXktbXVzdC1iZS0zMi1ieXRlcy1sb25n";

    @Test
    void generateAndValidateToken_returnsSubjectEmail() {
        JwtService jwtService = new JwtService(SECRET_BASE64, 3600000L);
        String token = jwtService.generateToken("prestamista@test.com");

        assertNotNull(token);
        assertEquals("prestamista@test.com", jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, "prestamista@test.com"));
        assertFalse(jwtService.isTokenValid(token, "otro@test.com"));
    }

    @Test
    void tamperedToken_throwsJwtException() {
        JwtService jwtService = new JwtService(SECRET_BASE64, 3600000L);
        String token = jwtService.generateToken("prestamista@test.com");
        String altered = token.substring(0, token.length() - 4) + "XXXX";

        assertThrows(JwtException.class, () -> jwtService.extractEmail(altered));
    }

    @Test
    void expiredToken_throwsJwtException() {
        JwtService jwtService = new JwtService(SECRET_BASE64, -1000L);
        String token = jwtService.generateToken("prestamista@test.com");

        assertThrows(JwtException.class, () -> jwtService.extractEmail(token));
    }
}
