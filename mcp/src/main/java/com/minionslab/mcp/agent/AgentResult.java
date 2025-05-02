package com.minionslab.mcp.agent;

import com.minionslab.mcp.step.StepExecution;
import java.util.Collections;
import java.util.List;

public class AgentResult {
    private final List<StepExecution> stepExecutions;

    public AgentResult(List<StepExecution> stepExecutions) {
        this.stepExecutions = stepExecutions == null ? Collections.emptyList() : stepExecutions;
    }

    public List<StepExecution> getStepExecutions() {
        return stepExecutions;
    }
} 