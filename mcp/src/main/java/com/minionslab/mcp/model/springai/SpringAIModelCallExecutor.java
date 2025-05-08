package com.minionslab.mcp.model.springai;

import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.memory.MCPChatMemory;
import com.minionslab.mcp.message.DefaultMCPMessage;
import com.minionslab.mcp.message.MessageRole;
import com.minionslab.mcp.model.*;
import com.minionslab.mcp.step.Step;
import com.minionslab.mcp.tool.impl.DestinationSearchTool;
import com.minionslab.mcp.tool.impl.PackagedTravelToolClient;
import com.minionslab.mcp.util.MCPtoSpringConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@Slf4j
public class SpringAIModelCallExecutor implements ModelCallExecutor {
    private final MCPModelCall modelCall;
    private final MCPContext context;
    private final Executor executor;
    private final ModelCallExecutionContext modelCallContext;
    private final BeanOutputConverter<Step.StepInstruction> converter;
    private final MCPChatMemory mcpChatMemory;
    private final ChatModel chatModel;
    
    public SpringAIModelCallExecutor(MCPModelCall modelCall, MCPContext context, ChatModel chatModel) {
        this(modelCall, context, chatModel, ForkJoinPool.commonPool());
    }
    
    public SpringAIModelCallExecutor(MCPModelCall modelCall, MCPContext context, ChatModel chatModel, Executor executor) {
        this.modelCall = modelCall;
        this.context = context;
        this.executor = executor;
        this.mcpChatMemory = context.getChatMemory();
        this.chatModel = chatModel;
        ToolCallingChatOptions chatOptions = (ToolCallingChatOptions) context.getModelConfig().getParameters().getOrDefault(
                "chatOptions", ToolCallingChatOptions.builder().build());
        this.modelCallContext = ModelCallExecutionContext.builder()
                .chatModel(chatModel)
                .chatMemory(null) // Provide ChatMemoryRepository if needed, or null if not used
                .conversationId(context.getAgentId())
                .chatOptions(chatOptions)
                .messageConverter(new MCPtoSpringConverter())
                .build();
        this.converter = modelCallContext.stepInstructionsConverter();
    }
    
    @Override
    public CompletableFuture<MCPModelCallResponse> execute() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MCPPrompt mcpPrompt = buildMCPPrompt();
                Prompt prompt = toSpringPrompt(mcpPrompt);
                var chatResponse = callModel(prompt);
                mcpChatMemory.saveChatResponse(modelCallContext.conversationId(), chatResponse);
                var response = new MCPModelCallResponse(chatResponse);
                modelCall.setResponse(response);
                finalizeModelCall(response);
                log.info("Model call execution completed successfully: {}", modelCall);
                return response;
            } catch (Exception e) {
                handleModelCallError(e);
                throw new ModelCallExecutionException(e.getMessage(), e);
            }
        }, executor);
    }
    
    MCPPrompt buildMCPPrompt() {
        log.info("Starting model call execution: {}", modelCall);
        if (!modelCall.getStatus().equals(ModelCallStatus.PENDING)) {
            throw new IllegalStateException("Invalid initial state. Model call must be in pending state");
        }
        modelCall.setStatus(ModelCallStatus.EXECUTING);
        List<Step> nextSteps = context.getStepManager().getPossibleNextSteps();
        StringBuilder stepInfo = new StringBuilder();
        if (!nextSteps.isEmpty()) {
            if (nextSteps.size() == 1) {
                Step nextStep = nextSteps.get(0);
                stepInfo.append("Next step:(step_id: ").append(nextStep.getId()).append(" - step_description:").append(nextStep.getDescription()).append(")\n");
            } else {
                stepInfo.append("Possible next steps:\n");
                for (Step step : nextSteps) {
                    stepInfo.append("- (step_id: ").append(step.getId()).append("- step_description: ").append(step.getDescription()).append(")\n");
                }
            }
        }
        List<com.minionslab.mcp.message.MCPMessage> memoryMessages = new java.util.ArrayList<>(mcpChatMemory.findByConversationId(modelCallContext.conversationId()));
        if (!nextSteps.isEmpty()) {
            String instruction = "INSTRUCTION: From the list of possible next steps, pick the next logical step and return its step_id in the nextStepSuggestion field of your " +
                                         "response.";
            memoryMessages.add(0, DefaultMCPMessage.builder().role(MessageRole.SYSTEM).content(instruction).build());
        }
        if (stepInfo.length() > 0) {
            memoryMessages.add(1, DefaultMCPMessage.builder().role(MessageRole.SYSTEM).content(stepInfo.toString()).build());
        }
        memoryMessages.add(DefaultMCPMessage.builder().role(MessageRole.SYSTEM).content(converter.getFormat()).build());
        // Build options map
        java.util.Map<String, Object> options = new java.util.HashMap<>();
        options.put("availableTools", context.getAvailableTools());
        options.put("chatOptions", modelCallContext.chatOptions());
        return new MCPPrompt(memoryMessages, options);
    }
    
    private Prompt toSpringPrompt(MCPPrompt mcpPrompt) {
        List<org.springframework.ai.chat.messages.Message> springMessages = MCPtoSpringConverter.toSpringMessages(mcpPrompt.getMessages());
        ToolCallingChatOptions chatOptions = modelCallContext.chatOptions().copy();
        chatOptions.setToolCallbacks(List.of(PackagedTravelToolClient.getcallback(), DestinationSearchTool.getcallback()));
        return new Prompt(springMessages, chatOptions);
    }
    
    ChatResponse callModel(Prompt prompt) {
        log.debug("Model call prompt messages: {}", prompt.getInstructions());
        log.debug("Model call parameters: {}", modelCall.getRequest().parameters());
        log.debug("Calling chat model for model call: {}", modelCall);
        return modelCallContext.chatModel().call(prompt);
    }
    
    private void finalizeModelCall(MCPModelCallResponse response) {
        modelCall.setResponse(response);
        modelCall.setStatus(ModelCallStatus.COMPLETED);
    }
    
    private void handleModelCallError(Exception e) {
        log.error("Model call execution failed: {}", e.getMessage(), e);
        modelCall.setStatus(ModelCallStatus.FAILED);
        modelCall.setError(new MCPModelCall.MCPModelCallError(
                MCPtoSpringConverter.createErrorMessage(e)
        ));
    }
} 