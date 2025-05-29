package com.minionslab.core.step.graph;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.step.Step;

import java.util.List;

public interface TransitionStrategy {
    Step selectNext(Step currentStep, List<Step> successors, AgentContext context);
}