package com.minionslab.api.test.security.service;

import org.springframework.security.core.Authentication;

public interface JwtTokenService {
    String generateToken(Authentication authentication);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
} 