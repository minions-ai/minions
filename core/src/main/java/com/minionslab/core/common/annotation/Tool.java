package com.minionslab.core.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as a tool that can be used by agents.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tool {
    /**
     * The unique identifier for the tool.
     * If empty, the method name will be used.
     */
    String id() default "";

    /**
     * The display name for the tool.
     * If empty, the method name will be used.
     */
    String name() default "";

    /**
     * A description of what the tool does.
     */
    String description();

    /**
     * Categories the tool belongs to.
     */
    String[] categories() default {};
} 