package com.minionslab.core.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a versioned bean.
 * This annotation can be used to specify the name and version of a bean.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VersionedBean {
    /**
     * The name of the versioned bean.
     * @return the name of the bean
     */
    String name();

    /**
     * The version of the versioned bean.
     * @return the version of the bean
     */
    String version();
} 