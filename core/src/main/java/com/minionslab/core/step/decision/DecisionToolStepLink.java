package com.minionslab.core.step.decision;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.step.Step;
import com.minionslab.core.tool.ToolCall;
import com.minionslab.core.tool.ToolCallExecutorFactory;

import java.util.List;

public class DecisionToolStepLink implements StepDecision {
    @Override
    public Step decideNextStep(Step currentStep, List<Step> possibleNextSteps, AgentContext context, ToolCallExecutorFactory toolCallExecutorFactory) {
        if (possibleNextSteps.size() > 1 && currentStep != null && currentStep.getDecisionToolCall().isPresent()) {
            ToolCall decisionCall = currentStep.getDecisionToolCall().get();
            String provider = context.getModelConfig() != null ? context.getModelConfig().getProvider() : "spring";
            var executor = toolCallExecutorFactory.forProvider(provider, decisionCall, context);
            ToolCall.ToolCallResponse response = executor.execute();
            String suggestedNextStepId = response != null ? response.response() : null;
            return possibleNextSteps.stream()
                                    .filter(s -> s.getId().equals(suggestedNextStepId))
                                    .findFirst()
                                    .orElse(null);
        }
        return null;
    }
} 