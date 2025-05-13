package com.minionslab.core.step.decision;

import com.minionslab.core.common.AbstractDecisionChain;
import com.minionslab.core.context.AgentContext;
import com.minionslab.core.step.Step;
import com.minionslab.core.tool.ToolCallExecutorFactory;

import java.util.List;

public class DefaultStepDecisionChain extends AbstractDecisionChain<StepDecision> {
    public DefaultStepDecisionChain() {
        this.chain.add(new SingleNextStepLink());
        this.chain.add(new DecisionToolStepLink());
        this.chain.add(new DefaultPolicyStepLink());
    }
    
    
    public Step decide(Step currentStep, List<Step> possibleNextSteps, AgentContext context, ToolCallExecutorFactory toolCallExecutorFactory) {
        for (StepDecision decision : chain) {
            Step result = decision.decideNextStep(currentStep, possibleNextSteps, context, toolCallExecutorFactory);
            if (result != null)
                return result;
        }
        return null;
    }
    // Implement next-step-specific logic here if needed
}
