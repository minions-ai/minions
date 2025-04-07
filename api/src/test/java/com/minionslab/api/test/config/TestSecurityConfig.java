package com.minionslab.api.test.config;

import static com.minionslab.core.test.TestConstants.TEST_ROLE_ADMIN;
import static com.minionslab.core.test.TestConstants.TEST_ROLE_USER;

import com.minionslab.core.context.MinionContextHolder;
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
            .requestMatchers(HttpMethod.POST, "/v1/prompts").hasRole(TEST_ROLE_USER)
            .requestMatchers(HttpMethod.POST, "/v1/prompts/{id}/components").hasRole(TEST_ROLE_USER)
            .requestMatchers(HttpMethod.PUT, "/v1/prompts/{id}/components/{componentType}").hasRole(TEST_ROLE_USER)
            .requestMatchers(HttpMethod.PUT, "/v1/prompts/{id}").hasRole(TEST_ROLE_USER)
            .requestMatchers(HttpMethod.DELETE, "/v1/prompts/{id}").hasRole(TEST_ROLE_ADMIN)
            .requestMatchers(HttpMethod.POST, "/v1/minions").hasRole(TEST_ROLE_USER)
            .requestMatchers(HttpMethod.PUT, "/v1/minions/{id}").hasRole(TEST_ROLE_USER)
            .requestMatchers(HttpMethod.DELETE, "/v1/minions/{id}").hasRole(TEST_ROLE_ADMIN)
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
              .roles(TEST_ROLE_ADMIN, TEST_ROLE_USER)
              .build();
        } else if ("user".equals(username)) {
          return User.builder()
              .username("user")
              .password(passwordEncoder.encode("password"))
              .roles(TEST_ROLE_USER)
              .build();
        }else if ("user2".equals(username)) {
          return User.builder()
              .username("user2")
              .password(passwordEncoder.encode("password"))
              .roles("none")
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