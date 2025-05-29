package com.minionslab.core.common.logging;

/**
 * Centralized logging topic constants for the framework.
 */
public final class LoggingTopics {
    public static final String MEMORY_STRATEGY = "MemoryStrategy";
    public static final String AGENT = "Agent";
    public static final String STEP = "Step";
    public static final String MODEL = "Model";
    public static final String TOOL = "Tool";
    public static final String CHAIN = "Chain";
    public static final String SERVICE = "Service";
    public static final String SECURITY = "Security";
    public static final String WORKFLOW = "Workflow";
    public static final String FALLBACK = "Fallback";

    private LoggingTopics() {} // Prevent instantiation
} 