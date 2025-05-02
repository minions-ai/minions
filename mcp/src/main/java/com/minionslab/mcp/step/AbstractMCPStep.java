package com.minionslab.mcp.step;

import com.minionslab.mcp.context.MCPContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Abstract base class for MCPStep implementations providing common functionality.
 */
@Getter
public abstract class AbstractMCPStep implements MCPStep {
    private final String id = UUID.randomUUID().toString();

    private final Set<String> requiredTools;
    private final String description;
    private StepExecution stepExecution;

    protected AbstractMCPStep(MCPContext context, String description) {
        this(context, new HashSet<>(), description);
    }

    protected AbstractMCPStep(MCPContext context, Set<String> requiredTools, String description) {

        this.requiredTools = new HashSet<>(requiredTools);
        this.description = description;
    }

    @Override
    public StepExecution getStepExecution() {
        return stepExecution;
    }

    @Override
    public void setStepExecution(StepExecution execution) {
        this.stepExecution = execution;
    }

    @Override
    public String toString() {
        return String.format("MCPStep{id='%s', description='%s', tools=%s}", 
            id, description, requiredTools);
    }
} 