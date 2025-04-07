package com.minionslab.api.test.security.config;

import com.minionslab.api.test.security.filter.DummyAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private DummyAuthenticationFilter dummyAuthenticationFilter;

    @Test
    void securityFilterChain_ShouldBeConfiguredCorrectly() throws Exception {
        // Act
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(null);

        // Assert
        assertNotNull(filterChain);
    }

    @Test
    void securityFilterChain_ShouldHaveDummyAuthenticationFilter() throws Exception {
        // Act
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(null);
        HttpSecurity http = (HttpSecurity) filterChain.getFilters().get(0);

        // Assert
        assertTrue(http.getSharedObject(UsernamePasswordAuthenticationFilter.class) instanceof DummyAuthenticationFilter);
    }

    @Test
    void securityFilterChain_ShouldHaveBasicAuthenticationFilter() throws Exception {
        // Act
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(null);
        HttpSecurity http = (HttpSecurity) filterChain.getFilters().get(0);

        // Assert
        assertTrue(http.getSharedObject(BasicAuthenticationFilter.class) instanceof BasicAuthenticationFilter);
    }
} 