package com.minionslab.core.common.security.validation;

import org.springframework.stereotype.Service;

@Service
public class TenantValidator {
    
    public boolean isValidTenant(String tenantId) {
        return tenantId != null && !tenantId.trim().isEmpty(); // Replace with actual check
    }
    
    public boolean isValidUser(String userId) {
        return userId != null && !userId.trim().isEmpty(); // Replace with actual check
    }
}
