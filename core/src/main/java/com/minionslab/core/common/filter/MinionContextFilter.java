package com.minionslab.core.common.filter;

import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.domain.MinionContext;
import com.minionslab.core.domain.MinionContextHolder;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(1) // Execute early in the filter chain
public class MinionContextFilter extends OncePerRequestFilter {

  MinionContextHolder contextHolder;

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

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    setupContextForRequest();

    // Continue with the filter chain
    filterChain.doFilter(request, response);
  }

  public void setupContextForRequest() {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !auth.isAuthenticated()) {
        throw new IllegalStateException("No authenticated user found");
      }

      // Create new parameters
      MinionContext context = new MinionContext(UUID.randomUUID().toString(), auth.getName(), extractTenantId(auth),
          extractEnvironmentId(auth), new ConcurrentHashMap<>());

      // Set initial request metadata
      context.addMetadata("requestTimestamp", System.currentTimeMillis());
      context.addMetadata("requestPath",
          ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURI());

      contextHolder.setContext(context);
      log.debug("Created parameters: {}", context.toString());

    } catch (Exception e) {
      log.error("Failed to setup parameters ", e);
      throw new MinionException.ContextCreationException("Failed to create minion parameters", e);
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    // Skip parameters creation for paths that don't need it
    return path.startsWith("/api/v1/health") ||
        path.startsWith("/api/v1/public");
  }

} 