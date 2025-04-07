package com.minionslab.core.domain.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Base class for API-based tools in the Minions framework.
 * Provides common functionality for making HTTP requests, handling responses,
 * and managing API-specific configurations.
 */
@Slf4j
@Component
public abstract class BaseAPIToolBox implements ToolBox {

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;


    @Getter
    protected final Map<String, String> headers = new ConcurrentHashMap<>();

    @Getter
    protected final Map<String, Object> config = new ConcurrentHashMap<>();

    /**
     * Makes an HTTP request with retry support
     *
     * @param url The URL to send the request to
     * @param method The HTTP method to use
     * @param body The request body (can be null)
     * @param responseType The expected response minionType
     * @return The response entity
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    protected <T> ResponseEntity<T> makeRequest(String url, HttpMethod method, Object body, Class<T> responseType) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::add);

        HttpEntity<Object> entity = new HttpEntity<>(body, httpHeaders);
        
        log.debug("Making {} request to {} with headers: {}", method, url, headers);
        
        return restTemplate.exchange(url, method, entity, responseType);
    }

    /**
     * Adds a header to be included in all requests
     *
     * @param name The header name
     * @param value The header value
     */
    protected void addHeader(String name, String value) {
        headers.put(name, value);
    }

    /**
     * Sets a configuration value
     *
     * @param key The configuration key
     * @param value The configuration value
     */
    protected void setConfig(String key, Object value) {
        config.put(key, value);
    }

    /**
     * Gets a configuration value
     *
     * @param key The configuration key
     * @param defaultValue The default value if not found
     * @return The configuration value
     */
    @SuppressWarnings("unchecked")
    protected <T> T getConfig(String key, T defaultValue) {
        return (T) config.getOrDefault(key, defaultValue);
    }

    /**
     * Validates the API configuration
     *
     * @throws IllegalStateException if the configuration is invalid
     */
    protected abstract void validateConfig();

    /**
     * Gets the base URL for the API
     *
     * @return The base URL
     */
    protected abstract String getBaseUrl();

    /**
     * Gets the API version
     *
     * @return The API version
     */
    protected abstract String getApiVersion();

    /**
     * Gets the full API URL for a given path
     *
     * @param path The API path
     * @return The full URL
     */
    protected String getApiUrl(String path) {
        String baseUrl = getBaseUrl();
        String version = getApiVersion();
        return String.format("%s/%s/%s", baseUrl, version, path);
    }
} 