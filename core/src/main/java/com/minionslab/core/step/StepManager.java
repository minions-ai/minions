package com.minionslab.core.step;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.agent.AgentRecipe;
import com.minionslab.core.step.graph.StepGraph;
import com.minionslab.core.step.graph.StepGraphCompletionStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * StepManager orchestrates the execution and progression of steps in an agent workflow.
 * <p>
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Extend StepManager to implement custom step progression, completion strategies, or workflow policies.</li>
 *   <li>Override methods to add logging, metrics, or advanced orchestration logic.</li>
 *   <li>Plug in custom {@link StepGraph} or {@link StepGraphCompletionStrategy} for advanced workflows.</li>
 * </ul>
 * <b>Usage:</b> Use StepManager to coordinate step execution, check workflow completion, and advance steps in agent orchestration.
 */
@Slf4j
public class StepManager {
    private final StepGraph stepGraph;
    private final StepGraphCompletionStrategy completionStrategy;
    
    
    private boolean workflowComplete;
    
    //todo complete the implementation of the completion strategies. In reality we should be using Completion Chain
    public StepManager(AgentRecipe recipe) {
        this.stepGraph = recipe.getStepGraph();
        this.completionStrategy = recipe.getCompletionStrategy();
        
    }
    
    
    public StepGraph getStepGraph() {
        return stepGraph;
    }
    
    public Step getCurrentStep() {
        return stepGraph.getCurrentStep();
    }
    
    
    public boolean isWorkflowComplete() {
        if (!workflowComplete && stepGraph.getCurrentStep() != null) {
            workflowComplete = completionStrategy.isComplete(stepGraph, stepGraph.getCurrentStep(), null);
        }
        return workflowComplete;
    }
    
    public void advanceToNextStep(AgentContext context) {
        stepGraph.advanceToNextStep(context);
    }
    
    public void setWorkflowComplete() {
        stepGraph.complete();
        this.workflowComplete = true;
        log.info("[StepManager] Workflow marked as complete.");
    }
    
    
}
