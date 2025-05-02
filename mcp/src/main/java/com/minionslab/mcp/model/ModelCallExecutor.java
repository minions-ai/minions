package com.minionslab.mcp.model;

import com.minionslab.mcp.context.MCPContext;
import com.minionslab.mcp.step.MCPStep;
import com.minionslab.mcp.util.MCPMessageSpringConverter;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Handles the execution of model calls, including message preparation,
 * prompt construction, and tool call extraction.
 */
@Slf4j
@Data
@Accessors(chain = true)
public class ModelCallExecutor {
    
    private final MCPModelCall modelCall;
    private final MCPContext context;
    private final Executor executor;
    private final ModelCallExecutionContext modelCallContext;
    private BeanOutputConverter<MCPStep.StepInstruction> converter;
    
    public ModelCallExecutor(MCPModelCall modelCall, MCPContext context) {
        this(modelCall, context, ForkJoinPool.commonPool());
    }
    
    public ModelCallExecutor(MCPModelCall modelCall,
                             MCPContext context,
                             Executor executor) {
        this.modelCall = modelCall;
        this.context = context;
        this.modelCallContext = context.getModelCallExecutionContext();
        this.converter = modelCallContext.converter();
        this.executor = executor;
    }
    
    /**
     * Creates a new executor for a model call.
     *
     * @param modelCall The model call to execute
     * @param context   The execution context
     * @return A new model call executor
     */
    public static ModelCallExecutor forCall(MCPModelCall modelCall,
                                            MCPContext context) {
        return new ModelCallExecutor(modelCall, context);
    }
    
    /**
     * Executes the model call asynchronously.
     *
     * @return A future containing the model call response
     */
    public CompletableFuture<MCPModelCall.MCPModelCallResponse> execute() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!modelCall.getStatus().equals(ModelCallStatus.PENDING)) {
                    throw new IllegalStateException("Invalid initial state. Model call must be in pending state");
                }
                modelCall.setStatus(ModelCallStatus.EXECUTING);
                
                // Convert MCP messages to Spring AI messages
                var springMessages = MCPMessageSpringConverter.toSpringMessages(modelCall.getRequest().messages());
                
                // Create initial prompt
                var initialPrompt = new Prompt(springMessages, modelCallContext.chatOptions());
                
                // Add to chat memory
                modelCallContext.chatMemory().saveAll(modelCallContext.conversationId(), initialPrompt.getInstructions());
                
                // Call the model
                Prompt prompt = createBasePrompt();
                
                var chatResponse = modelCallContext.chatModel().call(prompt);
                
                MCPStep.StepInstruction stepInstruction = converter.convert(chatResponse.getResult().getOutput().getText());
                
                handleInstructions(stepInstruction);
                
                // Add response to chat memory
                List<Message> messageList = chatResponse.getResults().stream().map(generation -> (Message) generation.getOutput()).toList();
                modelCallContext.chatMemory().saveAll(modelCallContext.conversationId(),
                        messageList);
                
                
                // Create response
                var response = new MCPModelCall.MCPModelCallResponse(
                        MCPMessageSpringConverter.toMCPMessages(
                                messageList)
                );
                
                modelCall.setResponse(response);
                modelCall.setStatus(ModelCallStatus.COMPLETED);
                
                return response;
                
            } catch (Exception e) {
                log.error("Model call execution failed: {}", e.getMessage(), e);
                modelCall.setStatus(ModelCallStatus.FAILED);
                modelCall.setError(new MCPModelCall.MCPModelCallError(
                        MCPMessageSpringConverter.createErrorMessage(e)
                ));
                throw new ModelCallExecutionException(e.getMessage(), e);
            }
        }, executor);
    }
    
    private void handleInstructions(MCPStep.StepInstruction stepInstruction) {
        this.context.addInstruction(stepInstruction);
    }
    
    private @NotNull Prompt createBasePrompt() {
        
        
        Message baseMessage = new PromptTemplate("{format}", Map.of("format", converter.getFormat())).createMessage();
        
        List<Message> memoryMessages = modelCallContext.chatMemory().findByConversationId(modelCallContext.conversationId());
        memoryMessages.add(baseMessage);
        
        Prompt prompt = new Prompt(memoryMessages,
                modelCallContext.chatOptions());
        
        return prompt;
    }
} 