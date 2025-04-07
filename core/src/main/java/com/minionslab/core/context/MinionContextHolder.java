package com.minionslab.core.context;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Simple ThreadLocal holder for MinionContext.
 * Provides access to the current minion's parameters across the application.
 */
@Component
@RequestScope
public class MinionContextHolder {
    private static final ThreadLocal<MinionContext> contextHolder = new ThreadLocal<>();



    /**
     * Gets the current parameters
     */
    public static MinionContext getContext() {
        return contextHolder.get();
    }

    /**
     * Gets the current parameters, throwing an exception if no parameters is available
     */
    public static MinionContext getRequiredContext() {
        MinionContext context = getContext();
        if (context == null) {
            throw new IllegalStateException("No parameters available");
        }
        return context;
    }

    /**
     * Clears the current parameters
     */
    public static void clearContext() {
        contextHolder.remove();
    }



    public static void setContext(MinionContext context) {
        contextHolder.set(context);
    }
}