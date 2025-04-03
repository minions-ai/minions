package com.minionslab.core.service;

import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.common.exception.MinionException.ContextCreationException;
import com.minionslab.core.common.exception.MinionException.ContextMismatchException;
import com.minionslab.core.common.exception.MinionException.ContextNotFoundException;
import com.minionslab.core.domain.MinionContext;
import com.minionslab.core.domain.MinionContextHolder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j @Service public class ContextService {

  private final MinionContextHolder contextHolder;


  public ContextService(MinionContextHolder contextHolder) {
    this.contextHolder = contextHolder;
  }

  /**
   * Create and set parameters for a minion
   */
  public MinionContext createContext() throws ContextCreationException {

    Authentication auth = getAuthentication();
    validateAuthentication(auth);

    MinionContext context = new MinionContext(generateContextId(), extractUserId(auth), extractTenantId(auth), extractEnvironmentId(auth),
        new ConcurrentHashMap<>(new HashMap<>()));

    MinionContextHolder.setContext(context);
    return context;
  }

  private String extractEnvironmentId(Authentication auth) {
    if (!(auth.getDetails() instanceof Map)) {
      return "default";
    }
    Map<?, ?> details = (Map<?, ?>) auth.getDetails();
    Object envId = details.get("environmentId");
    return envId != null ? envId.toString() : "default";
  }

  private String extractUserId(Authentication auth) {
    return (String) auth.getPrincipal();
  }

  /**
   * Get parameters for a minion
   */
  public MinionContext getContext(String minionId) throws ContextNotFoundException, ContextMismatchException {
    MinionContext context = contextHolder.getContext();

    if (context == null) {
      throw new MinionException.ContextNotFoundException("No parameters found");
    }

    return context;
  }

  /**
   * Update parameters metadatas
   */
  public void updateContextMetadata(String minionId, String key, Object value) {
    MinionContext context = null;
    try {
      context = getContext(minionId);
    } catch (ContextNotFoundException e) {
      throw new RuntimeException(e);
    } catch (ContextMismatchException e) {
      throw new RuntimeException(e);
    }
    context.addMetadata(key, value);
    log.debug("Updated parameters metadatas for minion: {}, key: {}", minionId, key);
  }

  /**
   * Clear parameters
   */
  public void clearContext() {
    contextHolder.clearContext();
    log.debug("Cleared parameters");
  }

  private Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  private void validateAuthentication(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user found");
    }
  }

  private String generateContextId() {
    return UUID.randomUUID().toString();
  }

  private String extractTenantId(Authentication auth) {
    if (!(auth.getDetails() instanceof Map)) {
      throw new IllegalStateException("No tenant ID found in authentication details");
    }
    Map<?, ?> details = (Map<?, ?>) auth.getDetails();
    Object tenantId = details.get("tenantId");
    if (tenantId == null) {
      throw new IllegalStateException("No tenant ID found in authentication details");
    }
    return tenantId.toString();
  }


} 