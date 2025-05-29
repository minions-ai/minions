package com.minionslab.core.step;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.chain.ProcessResult;
import com.minionslab.core.memory.MemoryManager;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.model.ModelCallStatus;
import com.minionslab.core.model.Prompt;
import com.minionslab.core.tool.ToolCall;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Add fields for custom step execution metadata, tracking, or orchestration.</li>
 *   <li>Override or extend methods to support custom result aggregation, error handling, or memory management.</li>
 *   <li>Use as the main context object for step processors and chains.</li>
 * </ul>
 * <b>Usage:</b> StepContext is the primary carrier for step execution state, results, and metadata. Extend for advanced step orchestration and tracking.
 */
@Data
@Accessors(chain = true)
public class StepContext implements ProcessContext {
    private final Step step;
    
    
    // StepExecution fields
    private final String id = UUID.randomUUID().toString();
    private final Instant startedAt;
    private StepCompletionOutputInstructions.StepCompletionInstruction completionResult;
    private Instant completedAt;
    private StepStatus status;
    private String error;
    private List<ModelCall> modelCalls = new ArrayList<>();
    private List<ToolCall> toolCalls = new ArrayList<>();
    private List<ProcessResult> results;
    private Map<String, Object> metadata = new java.util.HashMap<>();
    private int modelCallNumbers;
    
    public StepContext(Step step) {
        this.step = step;
        this.startedAt = Instant.now();
        this.status = StepStatus.IN_PROGRESS;
        this.results = new ArrayList<>();
        
    }
    
    
    public MemoryManager getMemoryManager() {
        return null;
    }
    
    public void setPrompt(Prompt prompt) {
        
    }
    
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public void addModelCall(ModelCall modelCall) {
        this.modelCalls.add(modelCall);
    }
    
    public void increaseModelCalls() {
        this.modelCallNumbers++;
    }
    
    public List<ModelCall> getUnfinishedModelCalls() {
        return modelCalls.stream().filter(modelCall -> modelCall.getStatus().equals(ModelCallStatus.PENDING)).collect(Collectors.toUnmodifiableList());
    }
    
    public List<ToolCall> unfinishedToolCalls() {
        return toolCalls.stream().filter(toolCall -> toolCall.getStatus().equals(ModelCallStatus.PENDING)).collect(Collectors.toUnmodifiableList());
    }
    
    @Override
    public void addResult(ProcessResult result) {
    
    }
}