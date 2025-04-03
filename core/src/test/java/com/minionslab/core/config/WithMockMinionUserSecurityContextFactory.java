package com.minionslab.core.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockMinionUserSecurityContextFactory implements WithSecurityContextFactory<WithMockMinionUser> {

  public SecurityContext createSecurityContext(WithMockMinionUser withUser) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();

    // Create authorities from roles
    SimpleGrantedAuthority[] authorities = new SimpleGrantedAuthority[withUser.roles().length];
    for (int i = 0; i < withUser.roles().length; i++) {
      authorities[i] = new SimpleGrantedAuthority("ROLE_" + withUser.roles()[i]);
    }

    // Create authentication details with tenant and environment info
    Map<String, Object> details = new HashMap<>();
    details.put("tenantId", withUser.tenantId());
    details.put("environmentId", withUser.environmentId());

    // Create the authentication token
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        withUser.username(),
        null,
        Arrays.asList(authorities)
    );
    authentication.setDetails(details);

    context.setAuthentication(authentication);
    return context;
  }
} 