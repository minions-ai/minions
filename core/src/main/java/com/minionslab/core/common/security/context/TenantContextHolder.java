package com.minionslab.core.common.security.context;

public class TenantContextHolder {
    private static final ThreadLocal<String> tenantId = new ThreadLocal<>();
    private static final ThreadLocal<String> userId = new ThreadLocal<>();
    
    public static void setTenantId(String id) { tenantId.set(id); }
    public static String getTenantId() { return tenantId.get(); }
    
    public static void setUserId(String id) { userId.set(id); }
    public static String getUserId() { return userId.get(); }
    
    public static void clear() {
        tenantId.remove();
        userId.remove();
    }
}

