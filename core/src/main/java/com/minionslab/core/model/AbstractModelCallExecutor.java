package com.minionslab.core.model;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.memory.ModelMemory;
import com.minionslab.core.message.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Generic abstract model call executor for any provider response type T.
 * Subclasses specify the provider response type and implement conversion logic.
 */
@Slf4j
public abstract class AbstractModelCallExecutor<T> implements ModelCallExecutor {
    protected final ModelCall modelCall;
    protected final AgentContext context;
    protected final ModelMemory modelMemory;
    
    public AbstractModelCallExecutor(ModelCall modelCall, AgentContext context) {
        this.modelCall = modelCall;
        this.context = context;
        this.modelMemory = context.getChatMemory();
    }
    
    @Override
    public CompletableFuture<ModelCallResponse> executeAsync() {
        return CompletableFuture.supplyAsync(() -> execute(), getExecutor());
    }
    
    @Override
    public ModelCallResponse execute() {
        try {
            log.info("Starting model call execution: {}", modelCall);
            if (!modelCall.getStatus().equals(ModelCallStatus.PENDING)) {
                throw new IllegalStateException("Invalid initial state. Model call must be in pending state");
            }
            modelCall.setStatus(ModelCallStatus.EXECUTING);
            Prompt prompt = buildPrompt();
            T rawResponse = callModel(prompt);
            handleProviderResponse(rawResponse); // Optional: for saving, logging, etc.
            ModelCallResponse response = toMCPModelCallResponse(rawResponse);
            modelCall.setResponse(response);
            finalizeModelCall(response);
            log.info("Model call execution completed successfully: {}", modelCall);
            return response;
        } catch (Exception e) {
            handleModelCallError(e);
            throw new ModelCallExecutionException(e.getMessage(), e);
        }
    }
    
    /**
     * Executes the provider-specific model call and returns the provider's response type.
     */
    protected abstract T callModel(Prompt prompt);
    
    /**
     * Converts the provider response to the framework's ModelCallResponse.
     */
    protected abstract ModelCallResponse toMCPModelCallResponse(T rawResponse);
    
    /**
     * Optionally handle the provider response (e.g., save to memory, log, etc.).
     * Default is no-op.
     */
    protected void handleProviderResponse(T rawResponse) {
    }
    
    protected Prompt buildPrompt() {
        ModelCall.ModelCallRequest request = modelCall.getRequest();
        List<Message> messages =
                request.messages();
        Map<String, Object> options = new HashMap<>();
        options.put("availableTools", context.getAvailableTools());
        options.put("chatOptions", context.getModelConfig().getParameters().get("chatOptions"));
        return new Prompt(messages, options, request.instructions());
    }
    
    protected void finalizeModelCall(ModelCallResponse response) {
        modelCall.setResponse(response);
        modelCall.setStatus(ModelCallStatus.COMPLETED);
    }
    
    protected void handleModelCallError(Exception e) {
        log.error("Model call execution failed: {}", e.getMessage(), e);
        modelCall.setStatus(ModelCallStatus.FAILED);
        modelCall.setError(new ModelCall.ModelCallError(
                com.minionslab.core.util.MessageConverter.createErrorMessage(e)
        ));
    }
    
    /**
     * Returns the executor to use for async execution.
     */
    protected abstract Executor getExecutor();
} 