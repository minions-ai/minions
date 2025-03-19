package com.minionslab.core.security.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class DummyAuthenticationFilter extends OncePerRequestFilter {

    private static final String DEFAULT_USER_ID = "test-user";
    private static final String DEFAULT_TENANT_ID = "test-tenant";
    private static final String DEFAULT_ENVIRONMENT_ID = "test-environment";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Create authentication details with tenant and environment info
            Map<String, Object> details = new HashMap<>();
            details.put("tenantId", DEFAULT_TENANT_ID);
            details.put("environmentId", DEFAULT_ENVIRONMENT_ID);

            // Create and set the authentication
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                DEFAULT_USER_ID,
                null,
                null
            );
            authentication.setDetails(details);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("Set dummy authentication with userId: {}, tenantId: {}, environmentId: {}", 
                DEFAULT_USER_ID, DEFAULT_TENANT_ID, DEFAULT_ENVIRONMENT_ID);
        } catch (Exception e) {
            log.error("Error setting up dummy authentication", e);
        }

        filterChain.doFilter(request, response);
    }
} 