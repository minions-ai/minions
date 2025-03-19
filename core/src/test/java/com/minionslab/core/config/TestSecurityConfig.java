package com.minionslab.core.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.Authentication;
import java.util.Collection;
import java.util.Collections;
import org.springframework.http.HttpMethod;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // Disable CSRF for testing
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/prompts/**").authenticated()  // Require authentication
            .requestMatchers(HttpMethod.POST, "/api/v1/prompts/**").hasRole("ADMIN")  // Require ADMIN for POST
            .requestMatchers(HttpMethod.PUT, "/api/v1/prompts/**").hasRole("ADMIN")   // Require ADMIN for PUT
            .requestMatchers(HttpMethod.DELETE, "/api/v1/prompts/**").hasRole("ADMIN") // Require ADMIN for DELETE
            .anyRequest().authenticated()
        )
        .httpBasic(Customizer.withDefaults());
    
    return http.build();
  }

  @Bean
  public SecurityContext testSecurityContext() {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(new TestAuthentication());
    return context;
  }

  // Custom Authentication class for testing
  private static class TestAuthentication implements Authentication {
    private final TestUserDetails principal;

    public TestAuthentication() {
      this.principal = new TestUserDetails();
    }

    @Override
    public Collection<? extends SimpleGrantedAuthority> getAuthorities() {
      return Collections.singletonList(new SimpleGrantedAuthority("ADMIN"));
    }

    @Override
    public Object getCredentials() {
      return null;
    }

    @Override
    public Object getDetails() {
      return null;
    }

    @Override
    public Object getPrincipal() {
      return principal;
    }

    @Override
    public boolean isAuthenticated() {
      return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
    }

    @Override
    public String getName() {
      return "testUser";
    }
  }

  // Custom UserDetails class containing your required fields
  private static class TestUserDetails {
    private final String userId = "test-user-id";
    private final String tenantId = "test-tenant-id";
    private final String environmentId = "test-env-id";

    public String getUserId() {
      return userId;
    }

    public String getTenantId() {
      return tenantId;
    }

    public String getEnvironmentId() {
      return environmentId;
    }
  }
} 