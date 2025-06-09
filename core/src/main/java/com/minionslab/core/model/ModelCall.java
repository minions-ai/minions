package com.minionslab.core.model;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.chain.ProcessResult;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.tool.ToolCall;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Extend ModelCall to add custom fields, metadata, or orchestration logic for model invocations.</li>
 *   <li>Override methods to support advanced error handling, result aggregation, or tool call integration.</li>
 *   <li>Use as the main carrier for model invocation state and results in the workflow.</li>
 * </ul>
 * <b>Usage:</b> ModelCall represents a single model invocation, including request, response, status, and tool calls. Extend for advanced orchestration or tracking.
 */
@Data
@Accessors(chain = true)
public class ModelCall implements ProcessContext {
    
    private final ModelConfig modelConfig;
    private ModelCallRequest request;
    private ModelCallStatus status;
    private ModelCallResponse response;
    private ModelCallError error;
    private List<ToolCall> toolCalls = new ArrayList<>();
    
    //todo figure out how to pass the model config around, and what to do if there is no ModelConfig
    public ModelCall(ModelConfig modelConfig, MessageBundle bundle) {
        this(modelConfig, bundle, Map.of());
    }
    
    public ModelCall(ModelConfig modelConfig, MessageBundle bundle, Map<String, Object> parameters) {
        this(modelConfig, bundle, parameters, null);
    }
    
    // New: Construct from MessageBundle
    public ModelCall(ModelConfig modelConfig, MessageBundle bundle, Map<String, Object> parameters, OutputInstructions instructions) {
        this(modelConfig, new ModelCallRequest(bundle.getAllMessages(), parameters, instructions));
    }
    
    public ModelCall(ModelConfig modelConfig, ModelCallRequest request) {
        this.modelConfig = modelConfig;
        this.request = request;
        this.status = ModelCallStatus.PENDING;
    }
    
    
    @Override
    public String toString() {
        return "ModelCall{" +
                       "request=" + request +
                       ", response=" + response +
                       ", status=" + status +
                       '}';
    }
    
    public ModelCallRequest getRequest() {
        return request;
    }
    
    public ModelCallResponse getResponse() {
        return response;
    }
    
    public void setResponse(ModelCallResponse response) {
        this.response = response;
    }
    
    public ModelCallStatus getStatus() {
        return status;
    }
    
    public void setStatus(ModelCallStatus status) {
        this.status = status;
    }
    
    public ModelCallError getError() {
        return error;
    }
    
    public void setError(ModelCallError error) {
        this.error = error;
    }
    
    public ModelConfig getModelConfig() {
        return modelConfig;
    }
    
    @Override
    public List getResults() {
        return List.of();
    }
    
    @Override
    public void addResult(ProcessResult result) {
    
    }
    
    
    public record ModelCallRequest(List<Message> messages, Map<String, Object> parameters, OutputInstructions instructions) {
    }
    
    public record ModelCallError(Message error) {
    }
    
    
}

