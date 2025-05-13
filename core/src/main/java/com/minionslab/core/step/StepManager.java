package com.minionslab.core.step;

import com.minionslab.core.agent.AgentRecipe;
import com.minionslab.core.context.AgentContext;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized step management for MCP workflows.
 * Owns step list, current step, step graph, instructions, and workflow completion state.
 */
@Slf4j
@Data
@Accessors(chain = true)
public class StepManager {
    private final StepGraph stepGraph;
    private Step currentStep;
    private boolean workflowComplete = false;
    
    public StepManager(AgentRecipe recipe) {
        this(recipe.getStepGraph());
    }
    
    public StepManager(StepGraph stepGraph) {
        this.stepGraph = stepGraph;
        this.currentStep = stepGraph.getSteps().get(0);
        
        
    }
    
    public StepGraph getStepGraph() {
        return stepGraph;
    }
    
    public Step getCurrentStep() {
        return currentStep;
    }
    
    public void setCurrentStep(Step step) {
        this.currentStep = step;
        if (step == null) {
            this.workflowComplete = true;
            System.out.println("[StepManager] Workflow marked as complete (no more steps).");
        } else {
            this.workflowComplete = false;
        }
    }
    
    public void setCurrentStep(String stepId) {
        List<Step> allSteps = stepGraph.getSteps();
        allSteps.stream().filter(step -> step.getId().equals(stepId)).findFirst().ifPresentOrElse(
                step -> {
                    this.currentStep = step;
                    this.workflowComplete = false;
                },
                () -> {
                    this.currentStep = null;
                    this.workflowComplete = true;
                    System.out.println("[StepManager] Workflow marked as complete (no more steps).");
                }
                                                                                                 );
    }
    
    public boolean isWorkflowComplete() {
        return workflowComplete;
    }
    
    /**
     * Advances to the next step using the StepGraph.
     *
     * @param context                 The AgentContext for model/tool execution
     * @param toolCallExecutorFactory The factory to execute tool calls
     */
    public void advanceToNextStep(AgentContext context, com.minionslab.core.tool.ToolCallExecutorFactory toolCallExecutorFactory) {
        List<Step> possibleNext = stepGraph.getPossibleNextSteps(currentStep);
        Step nextStep = stepGraph.selectNextStep(currentStep, possibleNext, context, toolCallExecutorFactory);
        if (nextStep != null) {
            setCurrentStep(nextStep);
        } else {
            setWorkflowComplete();
        }
    }
    
    public void setWorkflowComplete() {
        this.currentStep = null;
        this.workflowComplete = true;
        System.out.println("[StepManager] Workflow marked as complete (no more steps).");
    }
    
    public List<StepExecution> getStepExecutions() {
        List<StepExecution> executions = new ArrayList<>();
        for (Step step : stepGraph.getSteps()) {
            StepExecution exec = step.getStepExecution();
            if (exec != null)
                executions.add(exec);
        }
        return executions;
    }
} 