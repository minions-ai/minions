package com.minionslab.core.step.processor;

import com.minionslab.core.common.chain.AbstractProcessor;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.message.Message;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.step.StepContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PreparationProcessor extends AbstractProcessor<StepContext, StepContext> implements StepProcessor {
    @Override
    protected StepContext doProcess(StepContext input) throws Exception {
        Message systemPrompt = input.getStep().getSystemPrompt();
        Message userRequest = input.getUserRequest();
        MessageBundle messageBundle = new MessageBundle(List.of(systemPrompt, userRequest));
        input.getMemoryManager().query(getMemoryQuery());
        ModelCall modelCall = new ModelCall(input.getModelCallConfig(), messageBundle);
        input.getModelCalls().add(modelCall);
        return input;
    }
    
    //todo implement the method
    private MemoryQuery getMemoryQuery() {
        return null;
    }
}
