package com.minionslab.core.common.util;

import com.minionslab.core.common.exception.MissingContextException;
import com.minionslab.core.context.MinionContext;
import com.minionslab.core.context.MinionContextHolder;

public final class ContextUtils {
    private ContextUtils() {
        // Utility class, prevent instantiation
    }

    /**
     * Ensures that a MinionContext exists in the current thread.
     *
     * @throws MissingContextException if no context is found
     */
    public static void ensureContext() {
        MinionContext context = MinionContextHolder.getContext();
        if (context == null) {
            throw new MissingContextException("MinionContext is required but not found in the current thread");
        }
    }

    /**
     * Gets the current MinionContext or throws an exception if not found.
     *
     * @return the current MinionContext
     * @throws MissingContextException if no context is found
     */
    public static MinionContext getRequiredContext() {
        MinionContext context = MinionContextHolder.getContext();
        if (context == null) {
            throw new MissingContextException("MinionContext is required but not found in the current thread");
        }
        return context;
    }

    /**
     * Gets the current tenant ID or throws an exception if not found.
     *
     * @return the current tenant ID
     * @throws MissingContextException if no context or tenant ID is found
     */
    public static String getRequiredTenantId() {
        MinionContext context = getRequiredContext();
        String tenantId = context.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new MissingContextException("Tenant ID is required but not found in the current context");
        }
        return tenantId;
    }
} 