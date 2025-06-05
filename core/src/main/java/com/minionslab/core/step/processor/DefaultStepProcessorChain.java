package com.minionslab.core.step.processor;

import com.minionslab.core.common.chain.AbstractBaseChain;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.chain.ProcessorCustomizer;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.StepService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultStepProcessorChain extends AbstractBaseChain<StepProcessor, StepContext> {
    
    private final PlannerStepProcessor plannerStepProcessor;
    private final ModelCallStepProcessor modelCallStepProcessor;
    private final ToolCallStepProcessor toolCallStepProcessor;
    private final StepCompletionProcessor stepCompletionProcessor;
    private final PreparationProcessor preparationProcessor;
    
    
    /**
     * Constructs a new AbstractBaseChain with optional providers for processors and customizers.
     * Subclasses can use these providers to inject dependencies or discover available processors.
     *
     * @param processorProviders  provider for the initial list of processors
     * @param customizerProviders provider for the initial list of processor customizers
     * @param stepService
     */
    public DefaultStepProcessorChain(ObjectProvider<List<StepProcessor>> processorProviders, ObjectProvider<List<ProcessorCustomizer>> customizerProviders,
                                     StepService stepService, PlannerStepProcessor plannerStepProcessor, ModelCallStepProcessor modelCallStepProcessor,
                                     ToolCallStepProcessor toolCallStepProcessor, StepCompletionProcessor stepCompletionProcessor, PreparationProcessor preparationProcessor) {
        super(processorProviders, customizerProviders);
        this.plannerStepProcessor = plannerStepProcessor;
        this.modelCallStepProcessor = modelCallStepProcessor;
        this.toolCallStepProcessor = toolCallStepProcessor;
        this.stepCompletionProcessor = stepCompletionProcessor;
        this.preparationProcessor = preparationProcessor;
    }
    
    @Override
    protected void registerProcessors() {
        this.addToStart(preparationProcessor)
            .addToEnd(plannerStepProcessor)
            .addToEnd(modelCallStepProcessor)
            .addToEnd(toolCallStepProcessor)
            .addToEnd(stepCompletionProcessor);
        
    }
    
    @Override
    public boolean accepts(ProcessContext context) {
        return context instanceof StepContext;
    }
}
