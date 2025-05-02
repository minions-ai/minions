package com.minionslab.mcp.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.model.MCPModelCall;
import com.minionslab.mcp.step.MCPStep;
import com.minionslab.mcp.step.StepExecution;
import com.minionslab.mcp.step.StepExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Slf4j
public class AgentExecutor {
    private final AgentRecipe recipe;
    private final MCPContext context;
    
    public AgentExecutor(MCPAgent agent) {
        this.recipe = agent.recipe;
        context = MCPContext.builder()
                            .agentId(agent.agentId)
                            .modelConfig(agent.recipe.getModelConfig())
                            .executionSteps(recipe.getSteps())
                            .build();
        // Set execution parameters in metadata if not already set
        Map<String, Object> metadata = context.getMetadata();
        metadata.putIfAbsent("maxModelCallsPerStep", 10);
        metadata.putIfAbsent("maxToolCallRetries", 2);
        metadata.putIfAbsent("sequentialToolCalls", false);
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
            List<StepExecution> executions = new ArrayList<>();
            List<MCPStep> remainingSteps = new ArrayList<>(context.getExecutionSteps());
            Set<String> executedStepIds = new HashSet<>();
            
            while (!remainingSteps.isEmpty()) {
                // Get the next step to execute (either from LLM suggestion or sequential)
                MCPStep currentStep;
                if (context.getNextStep().isPresent()) {
                    MCPStep suggestedStep = context.getNextStep().get();
                    // Clear the suggestion after reading it
                    context.clearNextStep();
                    
                    // Verify the suggested step exists and hasn't been executed
                    if (remainingSteps.contains(suggestedStep) && !executedStepIds.contains(suggestedStep.getId())) {
                        currentStep = suggestedStep;
                        remainingSteps.remove(suggestedStep);
                        log.debug("Executing LLM-suggested step: {}", suggestedStep.getId());
                    } else {
                        // If suggested step is invalid, take the next sequential step
                        currentStep = remainingSteps.remove(0);
                        log.debug("Invalid step suggestion, falling back to sequential step: {}", currentStep.getId());
                    }
                } else {
                    // No suggestion, take the next sequential step
                    currentStep = remainingSteps.remove(0);
                    log.debug("Executing sequential step: {}", currentStep.getId());
                }
                
                // Execute the step
                try {
                    StepExecutor stepExecutor = new StepExecutor(currentStep, context);
                    StepExecution stepExecution = stepExecutor.execute().join();
                    executions.add(stepExecution);
                    executedStepIds.add(currentStep.getId());
                    
                    // Process any instructions from the step execution that might suggest next steps
                    processStepCompletionInstructions(stepExecution);
                    
                } catch (Exception e) {
                    log.error("Error executing step {}: {}", currentStep.getId(), e.getMessage());
                    throw new AgentExecutionException("Failed to execute step: " + currentStep.getId(), e);
                }
                
                // Safety check - prevent infinite loops
                if (executions.size() > context.getExecutionSteps().size() * 2) {
                    String message = "Too many step executions - possible infinite loop detected";
                    log.error(message);
                    throw new AgentExecutionException(message);
                }
            }
            
            log.info("Agent execution completed successfully with {} steps", executions.size());
            return new AgentResult(executions);
        }, ForkJoinPool.commonPool());
    }
    
    /**
     * Processes step completion instructions from the last model call of a step execution.
     * Updates the context with any next step suggestions found in the instructions.
     *
     * @param stepExecution The completed step execution to process
     */
    private void processStepCompletionInstructions(StepExecution stepExecution) {
        if (stepExecution.getModelCalls().isEmpty()) {
            return;
        }
        
        MCPModelCall lastModelCall = stepExecution.getModelCalls().get(stepExecution.getModelCalls().size() - 1);
        lastModelCall.getToolCalls().stream()
                     .filter(tc -> "step_completed".equals(tc.getName()))
                     .findFirst()
                     .ifPresent(tc -> {
                         try {
                             ObjectMapper mapper = new ObjectMapper();
                             Map<String, Object> params = mapper.readValue(tc.getRequest().parameters(), Map.class);
                             String nextStepId = (String) params.get("nextStepSuggestion");
                             if (nextStepId != null) {
                                 context.setNextStep(nextStepId);
                                 log.debug("Found next step suggestion: {}", nextStepId);
                             }
                         } catch (Exception e) {
                             // Log but continue execution
                             log.warn("Failed to process step completion instruction: {}", e.getMessage());
                         }
                     });
    }
} 