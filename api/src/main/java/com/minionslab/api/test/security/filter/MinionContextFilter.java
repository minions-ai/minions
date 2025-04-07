package com.minionslab.api.test.security.filter;

import com.minionslab.core.context.MinionContext;
import com.minionslab.core.context.MinionContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(1) // Execute early in the filter chain
public class MinionContextFilter extends OncePerRequestFilter {


  private static String extractTenantId(Authentication auth) {
    if (!(auth.getDetails() instanceof Map)) {
      throw new IllegalStateException("No tenant ID found");
    }
    Map<?, ?> details = (Map<?, ?>) auth.getDetails();
    Object tenantId = details.get("tenantId");
    if (tenantId == null) {
      throw new IllegalStateException("No tenant ID found");
    }
    return tenantId.toString();
  }

  private static String extractEnvironmentId(Authentication auth) {
    if (!(auth.getDetails() instanceof Map)) {
      return "default";
    }
    Map<?, ?> details = (Map<?, ?>) auth.getDetails();
    Object envId = details.get("environmentId");
    return envId != null ? envId.toString() : "default";
  }

  private final MinionContextHolder contextHolder;

  public MinionContextFilter(MinionContextHolder contextHolder) {
    this.contextHolder = contextHolder;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    try {
      setupContextForRequest();
      filterChain.doFilter(request, response);
    } finally {
      contextHolder.setContext(null);
    }
  }

  private void setupContextForRequest() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user found");
    }

    MinionContext context = new MinionContext(
        UUID.randomUUID().toString(),
        auth.getName(),
        extractTenantId(auth),
        extractEnvironmentId(auth),
        new ConcurrentHashMap<>()
    );

    contextHolder.setContext(context);
  }
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    // Skip parameters creation for paths that don't need it
    return path.startsWith("/api/v1/health") ||
        path.startsWith("/api/v1/public");
  }

} 