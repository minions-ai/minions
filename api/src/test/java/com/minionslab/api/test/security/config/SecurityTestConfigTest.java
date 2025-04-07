package com.minionslab.api.test.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityTestConfig.class)
class SecurityTestConfigTest {

    @Autowired
    private SecurityTestConfig securityTestConfig;

    @Test
    void securityFilterChain_ShouldBeConfiguredCorrectly() throws Exception {
        // Act
        SecurityFilterChain filterChain = securityTestConfig.securityFilterChain(null);

        // Assert
        assertNotNull(filterChain);
    }

    @Test
    void corsConfigurationSource_ShouldBeConfiguredCorrectly() {
        // Act
        CorsConfigurationSource corsConfigurationSource = securityTestConfig.corsConfigurationSource();

        // Assert
        assertNotNull(corsConfigurationSource);
    }

    @Test
    void passwordEncoder_ShouldBeConfiguredCorrectly() {
        // Act
        PasswordEncoder passwordEncoder = securityTestConfig.passwordEncoder();

        // Assert
        assertNotNull(passwordEncoder);
    }

    @Test
    void authenticationManager_ShouldBeConfiguredCorrectly() throws Exception {
        // Act
        AuthenticationManager authenticationManager = securityTestConfig.authenticationManager(null);

        // Assert
        assertNotNull(authenticationManager);
    }
} 