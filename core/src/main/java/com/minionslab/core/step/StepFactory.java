package com.minionslab.core.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.definition.StepDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StepFactory {
    
    private final ObjectMapper objectMapper;
    
    @Autowired
    public StepFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * <b>Extensibility:</b>
     * <ul>
     *   <li>Extend StepFactory to support custom step deserialization, registration, or construction logic.</li>
     *   <li>Override methods to add validation, error handling, or advanced step instantiation.</li>
     * </ul>
     * <b>Usage:</b> Use StepFactory to create steps from JSON definitions, supporting dynamic and pluggable workflows.
     *
     * @param json the JSON string representing the step definition
     * @return the built Step instance
     * @throws Exception if deserialization or step construction fails
     */
    public Step createStep(String json) throws Exception {
        StepDefinition<?> stepDefinition = objectMapper.readValue(json, StepDefinition.class);
        return stepDefinition.buildStep();
    }
}
