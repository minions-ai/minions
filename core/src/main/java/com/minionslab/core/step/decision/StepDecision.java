package com.minionslab.core.step.decision;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.step.Step;
import com.minionslab.core.tool.ToolCallExecutorFactory;
import java.util.List;

public interface StepDecision {
    /**
     * Decide the next step, or return null to pass to the next in the chain.
     */
    Step decideNextStep(
        Step currentStep,
        List<Step> possibleNextSteps,
        AgentContext context,
        ToolCallExecutorFactory toolCallExecutorFactory
    );
} 