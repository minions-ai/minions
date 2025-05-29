package com.minionslab.core.common.security.aspect;

import com.minionslab.core.common.security.context.TenantContextHolder;
import com.minionslab.core.common.security.validation.TenantValidator;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantAccessAspect {
    
    @Autowired
    private TenantValidator validator;
    
    @Pointcut("@annotation(com.myapp.security.annotation.TenantAware) || @within(com.myapp.security.annotation.TenantAware)")
    public void tenantScoped() {}
    
    @Pointcut("@annotation(com.myapp.security.annotation.UserScoped) || @within(com.myapp.security.annotation.UserScoped)")
    public void userScoped() {}
    
    @Before("tenantScoped()")
    public void checkTenant() {
        String tenantId = TenantContextHolder.getTenantId();
        if (!validator.isValidTenant(tenantId)) {
            throw new AccessDeniedException("Unauthorized tenant access: " + tenantId);
        }
    }
    
    @Before("userScoped()")
    public void checkUser() {
        String userId = TenantContextHolder.getUserId();
        if (!validator.isValidUser(userId)) {
            throw new AccessDeniedException("Unauthorized user access: " + userId);
        }
    }
}
