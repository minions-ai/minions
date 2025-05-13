package com.minionslab.core.model;

import com.minionslab.core.message.Message;
import com.minionslab.core.tool.ToolCall;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a model invocation request in the Model Context Protocol.
 */
@Data
@Accessors(chain = true)
public class ModelCall {
    private final ModelCallRequest request;
    private ModelCallStatus status;
    private ModelCallResponse response;
    private ModelCallError error;
    
    private List<ToolCall> toolCalls = new ArrayList<>();
    
    
    public ModelCall(ModelCallRequest request) {
        this.request = request;
        this.status = ModelCallStatus.PENDING;
    }
    
    // New: Construct from MessageBundle
    public ModelCall(MessageBundle bundle, Map<String, Object> parameters,OutputInstructions instructions) {
        this.request = new ModelCallRequest(bundle.getAllMessages(), parameters,instructions);
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
    
    public static ModelCall fromBundle(MessageBundle bundle) {
        return new ModelCall(bundle, Map.of());
    }
    
    public static ModelCall fromBundle(MessageBundle bundle, Map<String, Object> parameters) {
        return new ModelCall(bundle, parameters);
    }
    
    public record ModelCallRequest(List<Message> messages, Map<String, Object> parameters,OutputInstructions instructions) {
    }
    
    public record ModelCallError(Message error) {
    }
    

}

