package com.minionslab.core.step.processor;

import com.minionslab.core.common.chain.AbstractProcessor;
import com.minionslab.core.common.chain.ProcessResult;
import com.minionslab.core.common.chain.Processor;
import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.message.Message;
import com.minionslab.core.model.ModelCallStatus;
import com.minionslab.core.service.ModelCallService;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.impl.ModelCallStep;
import com.minionslab.core.tool.ToolCall;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ModelCallStepProcessor extends AbstractProcessor<StepContext,String> implements StepProcessor {
    
    private final ModelCallService modelService;
    
    public ModelCallStepProcessor(ModelCallService modelService) {
        this.modelService = modelService;
    }
    
    @Override
    public boolean accepts(StepContext input) {
        return !input.getUnfinishedModelCalls().isEmpty();
    }
    
    
    @Override
    public StepContext afterProcess(StepContext input) {
        
        input.getModelCalls().forEach(modelCall -> {
            List<ToolCall> toolCalls = modelCall.getToolCalls();
            input.getToolCalls().addAll(toolCalls);
        });
        return input;
    }
    
    @Override
    public StepContext onError(StepContext input, Exception e) {
        return super.onError(input, e);
    }
    
    @Override
    protected String doProcess(StepContext input) throws Exception {

        input.getUnfinishedModelCalls().forEach(modelCall -> {
            List<Message> messages = modelCall.getRequest().messages();
            Map<String, Object> parameters = modelCall.getRequest().parameters();
            ModelConfig modelConfig = modelCall.getModelConfig();
            modelCall = modelService.call(modelCall);
            modelCall.setStatus(ModelCallStatus.COMPLETED);
            input.increaseModelCalls();
        });
        return "completed";
        
    }
}
