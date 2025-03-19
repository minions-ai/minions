package com.minionslab.core.domain;

/**
 * Interface for entities that belong to a specific tenant
 */
public interface TenantAware {
    String getTenantId();
    void setTenantId(String tenantId);
}
