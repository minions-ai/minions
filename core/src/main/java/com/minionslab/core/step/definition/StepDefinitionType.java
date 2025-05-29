package com.minionslab.core.step.definition;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface StepDefinitionType {
    String type();
    String description() default "";
} 