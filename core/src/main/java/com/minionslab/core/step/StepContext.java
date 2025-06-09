package com.minionslab.core.step;

import com.minionslab.core.agent.AgentConfig;
import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.chain.ProcessResult;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.memory.MemoryManager;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.model.ModelCallStatus;
import com.minionslab.core.tool.ToolCall;
import com.minionslab.core.tool.ToolCallStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.*;
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
    private final ChainRegistry chainRegistry;
    
    
    // StepExecution fields
    private final String id = UUID.randomUUID().toString();
    private final Instant startedAt;
    private final AgentContext agentContext;
    private StepCompletionOutputInstructions completionResult;
    private Instant completedAt;
    private StepStatus status;
    private String error;
    private List<ModelCall> modelCalls = new ArrayList<>();
    private List<ToolCall> toolCalls = new ArrayList<>();
    private List<ProcessResult> results;
    private Map<String, Object> metadata = new HashMap<>();
    private int modelCallNumbers;
    private Message userRequest;
    private ModelConfig modelCallConfig;
    @NotNull
    private MemoryManager memoryManager;
    
    public StepContext(AgentContext agentContext, Step step) {
        this.agentContext = agentContext;
        this.userRequest = agentContext.getAgent().getUserRequest();
        this.step = step;
        this.startedAt = Instant.now();
        this.status = StepStatus.IN_PROGRESS;
        this.results = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.memoryManager = agentContext.getMemoryManager();
        this.chainRegistry = agentContext.getChainRegistry();
    }
    
    public static AgentConfig getConfig() {
        return AgentContext.getConfig();
    }
    
    public String getConversationId(){
        return agentContext.getConversationId();
    }
    
    

    
    public void addModelCall(ModelCall modelCall) {
        this.modelCalls.add(modelCall);
    }
    
    public void increaseModelCalls() {
        this.modelCallNumbers++;
    }
    
    public List<ModelCall> getUnfinishedModelCalls() {
        return modelCalls.stream().filter(modelCall -> modelCall.getStatus().equals(ModelCallStatus.PENDING)).toList();
    }
    
    public List<ToolCall> unfinishedToolCalls() {
        return toolCalls.stream().filter(toolCall -> toolCall.getStatus().equals(ToolCallStatus.PENDING)).toList();
    }
    
    @Override
    public void addResult(ProcessResult result) {
        results.add(result);
    }
    
    
}