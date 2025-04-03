package com.minionslab.core.config;

import com.minionslab.core.domain.MinionContextHolder;
import com.minionslab.core.test.TestConstants;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // Disable CSRF for testing
        .authorizeHttpRequests(auth -> auth
            // Admin-only endpoints (POST, PUT, DELETE)
            .requestMatchers(HttpMethod.POST, "/v1/prompts").authenticated()
            .requestMatchers(HttpMethod.POST, "/v1/prompts/{id}/components").authenticated()
            .requestMatchers(HttpMethod.PUT, "/v1/prompts/{id}").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/v1/prompts/{id}").authenticated()
            .requestMatchers(HttpMethod.POST, "/v1/minions").authenticated()
            .requestMatchers(HttpMethod.PUT, "/v1/minions/{id}").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/v1/minions/{id}").authenticated()
            // Read-only endpoints for authenticated users (GET)
            .requestMatchers(HttpMethod.GET, "/v1/prompts/{id}").authenticated()
            .requestMatchers(HttpMethod.GET, "/v1/prompts").authenticated()
            .requestMatchers(HttpMethod.GET, "/v1/prompts/type/{type}").authenticated()
            .requestMatchers(HttpMethod.GET, "/v1/minions/{id}").authenticated()
            .requestMatchers(HttpMethod.GET, "/v1/minions").authenticated()
            // Default deny
            .anyRequest().denyAll()
        )
        .httpBasic(Customizer.withDefaults());
    
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    return new UserDetailsService() {
      @Override
      public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if ("admin".equals(username)) {
          return User.builder()
              .username("admin")
              .password(passwordEncoder.encode("password"))
              .roles(TestConstants.TEST_ROLE_ADMIN)
              .build();
        } else if ("user".equals(username)) {
          return User.builder()
              .username("user")
              .password(passwordEncoder.encode("password"))
              .authorities(TestConstants.TEST_ROLE_USER)
              .build();
        }
        throw new UsernameNotFoundException("User not found: " + username);
      }
    };
  }

  @Bean
  public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
    return new AuthenticationProvider() {
      @Override
      public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        if (passwordEncoder().matches(password, userDetails.getPassword())) {
          // Create authentication details with tenant and environment info
          Map<String, Object> details = new HashMap<>();
          details.put("tenantId", TestConstants.TEST_TENANT_ID);
          details.put("environmentId", TestConstants.TEST_ENVIRONMENT_ID);
          
          UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
          );
          token.setDetails(details);
          return token;
        }
        
        throw new AuthenticationException("Invalid password", null) {};
      }

      @Override
      public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
      }
    };
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Primary
  public MinionContextHolder minionContextHolder() {
    return new MinionContextHolder();
  }
} 