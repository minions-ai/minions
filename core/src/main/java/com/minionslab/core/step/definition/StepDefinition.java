package com.minionslab.core.step.definition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonIgnoreProperties({"description", "type"})
public interface StepDefinition<T extends Step> {
    T buildStep();
    @JsonProperty("type")
    String getType();
    @JsonProperty("description")
    String getDescription();
}

