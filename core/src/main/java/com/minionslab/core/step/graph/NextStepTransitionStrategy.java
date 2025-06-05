package com.minionslab.core.step.graph;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.step.Step;

import java.util.List;

public class NextStepTransitionStrategy implements TransitionStrategy {
    @Override
    public Step selectNext(Step currentStep, List<Step> successors, AgentContext context) {
        if (successors.size() > 0) {
            return successors.get(0);
        }
        return null;
    }
}
