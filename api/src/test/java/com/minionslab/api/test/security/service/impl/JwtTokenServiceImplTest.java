package com.minionslab.api.test.security.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceImplTest {

    private JwtTokenServiceImpl jwtTokenService;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenServiceImpl();
        authentication = new UsernamePasswordAuthenticationToken(
            "testUser",
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Act
        String token = jwtTokenService.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenService.validateToken(token));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtTokenService.generateToken(authentication);

        // Act
        boolean isValid = jwtTokenService.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtTokenService.validateToken("invalid-token");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void getUsernameFromToken_WithValidToken_ShouldReturnUsername() {
        // Arrange
        String token = jwtTokenService.generateToken(authentication);

        // Act
        String username = jwtTokenService.getUsernameFromToken(token);

        // Assert
        assertEquals("testUser", username);
    }

    @Test
    void getUsernameFromToken_WithInvalidToken_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () -> jwtTokenService.getUsernameFromToken("invalid-token"));
    }
} 