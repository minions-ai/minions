package com.minionslab.agents.smarthome;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.agent.processor.StepOrchestratorProcessor;
import com.minionslab.core.common.chain.AbstractBaseChain;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.chain.ProcessorCustomizer;
import com.minionslab.core.step.StepService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultAgentProcessorChain extends AbstractBaseChain<StepOrchestratorProcessor, AgentContext> {
    
    
    private final StepService stepService;
    
    /**
     * Constructs a new AbstractBaseChain with optional providers for processors and customizers.
     * Subclasses can use these providers to inject dependencies or discover available processors.
     *
     * @param processorProviders  provider for the initial list of processors
     * @param customizerProviders provider for the initial list of processor customizers
     */
    public DefaultAgentProcessorChain(ObjectProvider<List<StepOrchestratorProcessor>> processorProviders, ObjectProvider<List<ProcessorCustomizer>> customizerProviders, StepService stepService) {
        super(processorProviders, customizerProviders);
        
        this.stepService = stepService;
    }
    
    @Override
    protected void registerProcessors() {
        StepOrchestratorProcessor stepOrchestratorProcessor = new StepOrchestratorProcessor(stepService);
        this.addToStart(stepOrchestratorProcessor);
    }
    
    /**
     * Returns true if any processor in the chain accepts the given context.
     *
     * @param context the context to check
     * @return true if any processor accepts, false otherwise
     */
    @Override
    public boolean accepts(ProcessContext context) {
        return context instanceof AgentContext;
    }
}
