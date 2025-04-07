package com.minionslab.api.test.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Base test configuration class.
 * Provides common beans and settings for integration tests.
 */
@TestConfiguration
public class BaseTestConfig {

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Base URL for API endpoints
     */
    public static final String BASE_URL = "/api/v1";

    /**
     * Common test constants
     */
    public static class TestConstants {
        public static final String TEST_TENANT_ID = "test-tenant";
        public static final String TEST_USER_ID = "test-user";
        public static final String TEST_ADMIN_ID = "test-admin";
    }
} 