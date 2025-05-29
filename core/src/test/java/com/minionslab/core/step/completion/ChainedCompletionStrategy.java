package com.minionslab.core.step.completion;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.graph.StepGraph;
import com.minionslab.core.step.graph.StepGraphCompletionStrategy;

public class ChainedCompletionStrategy implements StepGraphCompletionStrategy {
    @Override
    public boolean isComplete(StepGraph graph, Step currentStep, AgentContext context) {
        return false;
    }
}
