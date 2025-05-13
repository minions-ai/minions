package com.minionslab.core.step.decision;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.step.Step;
import com.minionslab.core.tool.ToolCallExecutorFactory;
import java.util.List;

public class SingleNextStepLink implements StepDecision {
    @Override
    public Step decideNextStep(Step currentStep, List<Step> possibleNextSteps, AgentContext context, ToolCallExecutorFactory toolCallExecutorFactory) {
        if (possibleNextSteps.size() == 1) {
            return possibleNextSteps.get(0);
        }
        return null;
    }
} 