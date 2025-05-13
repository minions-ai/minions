package com.minionslab.core.step.completion;

import com.minionslab.core.common.AbstractDecisionChain;
import com.minionslab.core.step.StepExecution;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultStepCompletionChain extends AbstractDecisionChain<StepCompletionLink> {
    public DefaultStepCompletionChain(int maxModelCalls) {
        this.chain.add(new ModelCompletionMarkerLink());
        this.chain.add(new MaxModelCallCountLink(maxModelCalls));
        this.chain.add(new ToolOutcomeLink());
        this.chain.add(new FallbackLink());
    }
    
    
    public StepCompletionResult isComplete(StepExecution execution) {
        for (StepCompletionLink link : chain) {
            StepCompletionResult result = link.check(execution);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}