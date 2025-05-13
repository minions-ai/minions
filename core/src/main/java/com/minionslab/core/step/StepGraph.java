package com.minionslab.core.step;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.tool.ToolCallExecutorFactory;

import java.util.List;


public interface StepGraph {
    /**
     * Returns the possible next steps from the current step.
     */
    List<Step> getPossibleNextSteps(Step currentStep);
    
    /**
     * Selects the next step to execute, given the current step, context, and possible next steps.
     * This can use static logic, a decision chain, or delegate to a workflow engine.
     */
    Step selectNextStep(
            Step currentStep,
            List<Step> possibleNextSteps,
            AgentContext context,
            ToolCallExecutorFactory toolCallExecutorFactory
                       );
    
    /**
     * Returns all steps in the graph.
     */
    List<Step> getSteps();
    
    /**
     * Adds a step to the graph.
     */
    void addStep(Step step);
} 