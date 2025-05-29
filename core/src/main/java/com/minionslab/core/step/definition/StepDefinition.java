package com.minionslab.core.step.definition;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.minionslab.core.step.Step;

/**
 * StepDefinition defines the contract for serializable, pluggable step blueprints.
 * <p>
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Implement this interface to define new step types that can be built from configuration or JSON.</li>
 *   <li>Override default methods to provide custom type and description metadata.</li>
 * </ul>
 * <b>Usage:</b> Use StepDefinition to enable dynamic, configurable workflows and step instantiation.
 */
public interface StepDefinition<T extends Step> {
    T buildStep();
    
    default String getType() {
        StepDefinitionType ann = this.getClass().getAnnotation(StepDefinitionType.class);
        return ann != null ? ann.type() : this.getClass().getSimpleName();
    }
    
    default String getDescription() {
        StepDefinitionType ann = this.getClass().getAnnotation(StepDefinitionType.class);
        return ann != null ? ann.description() : "No description";
    }
}

