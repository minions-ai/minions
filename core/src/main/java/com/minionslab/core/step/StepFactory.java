package com.minionslab.core.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.step.definition.StepDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StepFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(StepFactory.class);
    
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
     * @throws StepException if deserialization or step construction fails
     */
    public Step createStep(String json) {
        try {
            StepDefinition<?> stepDefinition = objectMapper.readValue(json, StepDefinition.class);
            validateStepDefinition(stepDefinition);
            Step step = stepDefinition.buildStep();
            validateStep(step);
            logger.info("Step created: {} of type {}", step.getId(), step.getType());
            return step;
        } catch (Exception e) {
            logger.error("Failed to create step from JSON: {}", json, e);
            throw new StepException("Failed to create step: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the StepDefinition before building the Step.
     * @param def the StepDefinition to validate
     */
    protected void validateStepDefinition(StepDefinition<?> def) {
        if (def == null) {
            throw new StepException("StepDefinition is null");
        }
        if (def.getType() == null || def.getType().isEmpty()) {
            throw new StepException("StepDefinition type is missing");
        }
        // Add more validation as needed
    }

    /**
     * Validates the Step after building it.
     * @param step the Step to validate
     */
    protected void validateStep(Step step) {
        if (step.getId() == null || step.getId().isEmpty()) {
            throw new StepException("Step ID is missing");
        }
        // Add more validation as needed
    }


    
}
