package com.minionslab.core.agent.processor;

import com.minionslab.core.agent.Agent;
import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.agent.AgentRecipe;
import com.minionslab.core.common.chain.ProcessResult;
import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.common.logging.LoggingTopics;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.StepService;
import com.minionslab.core.step.graph.StepGraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * StepOrchestratorProcessor orchestrates the execution of an agent's workflow in the MCP framework.
 * It implements the {@link com.minionslab.core.common.chain.Processor} interface, enabling
 * pluggable, chain-of-responsibility processing for agent contexts.
 * <p>
 * This class is designed for extensibility: you can override hooks for before/after/error,
 * customize the main process loop, or plug in custom chain registries and step orchestration logic.
 * <p>
 * StepOrchestratorProcessor coordinates step execution, error handling, and memory management for agents.
 */
@Slf4j(topic = LoggingTopics.AGENT)
@Component
public class StepOrchestratorProcessor implements Processor<AgentContext> {
    private final StepService stepService;
    
    @Autowired
    public StepOrchestratorProcessor(StepService stepService) {
        this.stepService = stepService;
    }
    
    /**
     * Pre-processing hook. Takes a memory snapshot before processing.
     *
     * @param input the agent context
     * @return the (possibly modified) agent context
     */
    @Override
    public AgentContext beforeProcess(AgentContext input) {
        Processor.super.beforeProcess(input);
        log.debug("[StepOrchestratorProcessor] Starting process for agent: {}", input.getAgent().getAgentId());
        input.getMemoryManager().snapshot();
        return input;
    }
    
    /**
     * Error handling hook. Restores memory snapshot on error.
     *
     * @param input the agent context
     * @param e     the exception thrown
     * @return the (possibly modified) agent context
     */
    @Override
    public AgentContext onError(AgentContext input, Exception e) {
        Processor.super.onError(input, e);
        log.error("[StepOrchestratorProcessor] Error processing agent: {}", input.getAgent().getAgentId(), e);
        input.getMemoryManager().restoreLatestSnapshot();
        return input;
    }
    
    /**
     * Post-processing hook. Flushes memory after processing.
     *
     * @param input the agent context
     * @return the (possibly modified) agent context
     */
    @Override
    public AgentContext afterProcess(AgentContext input) {
        Processor.super.afterProcess(input);
        log.debug("[StepOrchestratorProcessor] Completed process for agent: {}", input.getAgent().getAgentId());
        //todo what should we do with the memory when the agent is done?
        input.getMemoryManager().flush();
        return input;
    }
    
    /**
     * Determines if this processor accepts the given agent context.
     *
     * @param input the agent context
     * @return true if accepted
     */
    @Override
    public boolean accepts(AgentContext input) {
        return input != null;
    }
    
    /**
     * Main process loop for agent execution. Orchestrates step execution using StepService.
     *
     * @param input the agent context
     * @return the processed agent context
     */
    @Override
    public AgentContext process(AgentContext input) {
        log.info("[StepOrchestratorProcessor] Processing agent workflow for agent: {}", input.getAgent().getAgentId());
        Agent agent = input.getAgent();
        List<ProcessResult> agentResult = input.getResults();
        AgentRecipe recipe = input.getRecipe();
        StepGraph stepGraph = recipe.getStepGraph();
        Step currentStep = stepGraph.getCurrentStep();
        while (currentStep != null) {
            StepContext stepContext = createStepContext(input, currentStep);
            StepContext process = stepService.executeStep(stepContext);
            currentStep = stepGraph.getNextStep(input);
        }
        log.info("[StepOrchestratorProcessor] Agent workflow completed for agent: {}", input.getAgent().getAgentId());
        return input;
    }
    
    /**
     * Creates a StepContext for the given step. Subclasses can override for custom context logic.
     *
     * @param nextStep the next step
     * @return the step context
     */
    private StepContext createStepContext(AgentContext agentContext, Step nextStep) {
        return new StepContext(agentContext, nextStep);
    }
}
