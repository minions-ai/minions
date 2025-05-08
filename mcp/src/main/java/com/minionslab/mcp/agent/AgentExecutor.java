package com.minionslab.mcp.agent;

import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.memory.MCPChatMemory;
import com.minionslab.mcp.model.ModelCallExecutorFactory;
import com.minionslab.mcp.step.Step;
import com.minionslab.mcp.step.StepExecution;
import com.minionslab.mcp.step.StepExecutor;
import com.minionslab.mcp.step.StepManager;
import com.minionslab.mcp.tool.ToolCallExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Slf4j
public class AgentExecutor {
    private final AgentRecipe recipe;
    private final MCPContext context;
    private final StepManager stepManager;
    private ModelCallExecutorFactory modelCallExecutorFactory;
    private ToolCallExecutorFactory toolCallExecutorFactory;
    

    
    public AgentExecutor(MCPAgent agent, MCPContext agentContext, ModelCallExecutorFactory modelCallExecutorFactory, ToolCallExecutorFactory toolCallExecutorFactory) {
        this.recipe = agent.getRecipe();
        context = agentContext;
        stepManager = context.getStepManager();
        // Removed metadata initialization; now handled in MCPContext
        this.modelCallExecutorFactory = modelCallExecutorFactory;
        this.toolCallExecutorFactory = toolCallExecutorFactory;
    }
    
    /**
     * Synchronous convenience method.
     */
    public AgentResult executeSync() {
        return execute().join();
    }
    
    /**
     * Executes the agent asynchronously, handling dynamic step execution based on LLM suggestions.
     *
     * <p>Features:</p>
     * <ul>
     *   <li>Supports LLM-suggested next steps</li>
     *   <li>Prevents duplicate step execution</li>
     *   <li>Maintains execution order when no suggestions exist</li>
     *   <li>Includes safety checks against infinite loops</li>
     *   <li>Processes step completion instructions for next step suggestions</li>
     * </ul>
     *
     * @return A future containing the agent execution result
     */
    public CompletableFuture<AgentResult> execute() {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Starting agent execution for recipe: {}", recipe.getId());
            List<StepExecution> executions = new ArrayList<>();
            Set<String> executedStepIds = new HashSet<>();
            while (!stepManager.isWorkflowComplete()) {
                Step currentStep = stepManager.getCurrentStep();
                if (executedStepIds.contains(currentStep.getId()) && !context.allowRepeatedSteps) {
                    log.error("Step {} has already been executed. Possible loop detected.", currentStep.getId());
                    throw new AgentExecutionException("Step already executed: " + currentStep.getId());
                }
                try {
                    log.info("Starting execution of step: {}", currentStep.getId());
                    StepExecutor stepExecutor = createStepExecutor(currentStep);
                    StepExecution stepExecution = stepExecutor.execute().join();
                    executions.add(stepExecution);
                    executedStepIds.add(currentStep.getId());
                    log.info("Completed execution of step: {} with status: {}", currentStep.getId(), stepExecution.getStatus());
                } catch (Exception e) {
                    log.error("Error executing step {}: {}", currentStep.getId(), e.getMessage(), e);
                    throw new AgentExecutionException("Failed to execute step: " + currentStep.getId(), e);
                }
                // Advance to next step using centralized step manager
                stepManager.advanceToNextStep();
                // Safety check - prevent infinite loops
              /*  if (executedStepIds.size() > stepManager.getSteps().size() * 2) {
                    String message = "Too many step executions - possible infinite loop detected";
                    log.error(message);
                    throw new AgentExecutionException(message);
                }*/
            }
            log.info("Agent execution completed successfully with {} steps", executions.size());
            return new AgentResult(executions);
        }, ForkJoinPool.commonPool());
    }
    
    protected @NotNull StepExecutor createStepExecutor(Step currentStep) {
        return new StepExecutor(currentStep, context, modelCallExecutorFactory, toolCallExecutorFactory);
    }
} 