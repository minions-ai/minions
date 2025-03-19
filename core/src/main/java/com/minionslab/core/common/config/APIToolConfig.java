package com.minionslab.core.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for API-based tools.
 */
@Configuration
public class APIToolConfig {

    /**
     * Creates a RestTemplate bean with default configuration.
     * This bean will be used by BaseAPIToolBox and its subclasses for making HTTP requests.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 