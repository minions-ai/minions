package com.minionslab.mcp.step;

import com.minionslab.mcp.agent.AgentRecipe;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Centralized step management for MCP workflows.
 * Owns step list, current step, step graph, instructions, and workflow completion state.
 */
@Slf4j
public class StepManager {
    private final List<Step> steps;
    private final Map<String, List<String>> stepGraph;
    private final List<Step.StepInstruction> instructions = Collections.synchronizedList(new ArrayList<>());
    private final List<Step.StepInstruction> instructionsToExecute = Collections.synchronizedList(new ArrayList<>());
    private Step currentStep;
    private boolean workflowComplete = false;
    
    public StepManager(AgentRecipe recipe) {
        this(recipe.getSteps(), recipe.getStepGraph());
    }
    
    public StepManager(List<Step> steps, Map<String, List<String>> stepGraph) {
        this.steps = new ArrayList<>(steps);
        this.stepGraph = stepGraph != null ? new HashMap<>(stepGraph) : new HashMap<>();
        if (!steps.isEmpty()) {
            this.currentStep = steps.get(0);
        }
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
        steps.stream().filter(step -> step.getId().equals(stepId)).findFirst().ifPresentOrElse(
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
    
    public void setWorkflowComplete() {
        
        this.currentStep = null;
        this.workflowComplete = true;
        log.info("[StepManager] Workflow marked as complete (no more steps).");
    }
    
    public boolean isWorkflowComplete() {
        return workflowComplete;
    }
    
    public List<Step> getSteps() {
        return Collections.unmodifiableList(steps);
    }
    
    public List<Step.StepInstruction> getInstructions() {
        return instructions;
    }
    
    public void addInstruction(Step.StepInstruction stepInstruction) {
        this.instructions.add(stepInstruction);
        this.instructionsToExecute.add(stepInstruction);
    }
    
    /**
     * Determines and advances to the next step, if any. Returns the new current step, or null if complete.
     */
    public Step advanceToNextStep() {
        if (workflowComplete)
            return null;
        List<Step> possibleNext = getPossibleNextSteps();
        if (possibleNext.size() == 1) {
            setCurrentStep(possibleNext.get(0));
            return currentStep;
        } else if (possibleNext.size() > 1) {
            for (Step.StepInstruction stepInstruction : getInstructionsToExecute()) {
                String suggestion = stepInstruction.nextStepSuggestion();
                if (suggestion != null && possibleNext.stream().anyMatch(step -> step.getId().equals(suggestion))) {
                    setCurrentStep(suggestion);
                    return currentStep;
                }
            }
        } else if (possibleNext.isEmpty()) {
            setCurrentStep((Step) null);
            return null;
        }
        // No valid next step found
        setCurrentStep((Step) null);
        return null;
    }
    
    public List<Step.StepInstruction> getInstructionsToExecute() {
        return instructionsToExecute;
    }
    
    public List<Step> getPossibleNextSteps() {
        if (currentStep == null)
            return Collections.emptyList();
        List<String> nextIds = stepGraph.getOrDefault(currentStep.getId(), List.of());
        return steps.stream()
                    .filter(step -> nextIds.contains(step.getId()))
                    .toList();
    }
    
    public void reset() {
        if (!steps.isEmpty()) {
            this.currentStep = steps.get(0);
            this.workflowComplete = false;
        } else {
            this.currentStep = null;
            this.workflowComplete = true;
        }
        this.instructions.clear();
        this.instructionsToExecute.clear();
    }
    
    public void addStep(Step step) {
        if (step == null)
            throw new IllegalArgumentException("Step cannot be null");
        this.steps.add(step);
    }
    
    public List<StepExecution> getStepExecutions() {
        List<StepExecution> executions = new ArrayList<>();
        for (Step step : steps) {
            StepExecution exec = step.getStepExecution();
            if (exec != null)
                executions.add(exec);
        }
        return executions;
    }
    
    // --- Encapsulation methods for context/step mutation ---
    
    /**
     * Removes an instruction from the instructionsToExecute list.
     */
    public void removeInstructionToExecute(Step.StepInstruction instruction) {
        this.instructionsToExecute.remove(instruction);
    }
    
    /**
     * Removes an instruction from the instructions list.
     */
    public void removeInstruction(Step.StepInstruction instruction) {
        this.instructions.remove(instruction);
    }
    
    /**
     * Alias for setCurrentStep(String stepId).
     */
    public void setCurrentStepById(String stepId) {
        setCurrentStep(stepId);
    }
    
    /**
     * Sets current step to null and marks workflow as complete.
     */
    public void setCurrentStepToNull() {
        setCurrentStep((Step) null);
    }
    
    /**
     * Returns the next step suggestion from instructions, if any, that matches possibleNext.
     */
    public String getNextStepSuggestion(List<Step> possibleNext, List<Step.StepInstruction> instructions) {
        for (Step.StepInstruction stepInstruction : instructions) {
            String suggestion = stepInstruction.nextStepSuggestion();
            if (suggestion != null && possibleNext.stream().anyMatch(step -> step.getId().equals(suggestion))) {
                return suggestion;
            }
        }
        return null;
    }
} 