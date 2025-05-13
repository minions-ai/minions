package com.minionslab.core.agent;

import com.minionslab.core.step.StepExecution;

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