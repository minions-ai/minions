package com.minionslab.core.agent;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import com.minionslab.core.model.ModelCallExecutorFactory;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.StepExecution;
import com.minionslab.core.step.StepExecutor;
import com.minionslab.core.step.StepManager;
import com.minionslab.core.tool.ToolCallExecutorFactory;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Slf4j
public class AgentExecutor {
    private final AgentContext context;
    private final StepManager stepManager;
    private final @NotBlank Agent agent;
    private ModelCallExecutorFactory modelCallExecutorFactory;
    private ToolCallExecutorFactory toolCallExecutorFactory;
    
    
    public AgentExecutor(AgentContext agentContext, ModelCallExecutorFactory modelCallExecutorFactory, ToolCallExecutorFactory toolCallExecutorFactory) {
        context = agentContext;
        stepManager = context.getStepManager();
        this.agent = context.getAgent();
        // Removed metadata initialization; now handled in AgentContext
        this.modelCallExecutorFactory = modelCallExecutorFactory;
        this.toolCallExecutorFactory = toolCallExecutorFactory;
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
    public CompletableFuture<AgentResult> executeAsync() {
        return CompletableFuture.supplyAsync(this::execute, ForkJoinPool.commonPool());
    }
    
    /**
     * Synchronous convenience method.
     */
    public AgentResult execute() {
        // Add agent's system and goal messages to memory before step execution
        String conversationId = agent.agentId;
        MessageBundle agentBundle = agent.getMessageBundle();
        // Agent system message


        // Agent goal message (if available)
        String agentGoal = null;
        if (context.getRecipe().getParameters() != null && context.getRecipe().getParameters().get("goal") != null) {
            agentGoal = context.getRecipe().getParameters().get("goal").toString();
        }
        if (agentGoal != null && !agentGoal.isEmpty()) {
            Map<String, Object> goalMeta = new java.util.HashMap<>();
            goalMeta.put("agentGoal", true);
            agentBundle.addMessage(DefaultMessage.builder()
                                                 .role(com.minionslab.core.message.MessageRole.SYSTEM)
                                                 .scope(com.minionslab.core.message.MessageScope.AGENT)
                                                 .content(agentGoal)
                                                 .metadata(goalMeta)
                                                 .build());
        }
        context.getChatMemory().saveAll(conversationId, agentBundle.getAllMessages());
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
                StepExecution stepExecution = stepExecutor.execute();
                executions.add(stepExecution);
                executedStepIds.add(currentStep.getId());
                log.info("Completed execution of step: {} with status: {}", currentStep.getId(), stepExecution.getStatus());
            } catch (Exception e) {
                log.error("Error executing step {}: {}", currentStep.getId(), e.getMessage(), e);
                throw new AgentExecutionException("Failed to execute step: " + currentStep.getId(), e);
            }
            // Advance to next step using centralized step manager
            stepManager.advanceToNextStep(context, toolCallExecutorFactory);
            // Safety check - prevent infinite loops
              /*  if (executedStepIds.size() > stepManager.getSteps().size() * 2) {
                    String message = "Too many step executions - possible infinite loop detected";
                    log.error(message);
                    throw new AgentExecutionException(message);
                }*/
        }
        log.info("Agent execution completed successfully with {} steps", executions.size());
        return new AgentResult(executions);
    }
    
    protected @NotNull StepExecutor createStepExecutor(Step currentStep) {
        return new StepExecutor(currentStep, context, modelCallExecutorFactory, toolCallExecutorFactory);
    }
} 