package com.minionslab.core.step;

import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.step.graph.StepGraph;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class StepServiceImpl implements StepService {
    
    
    private final ObjectProvider<ChainRegistry> chainRegistryProvider;
    
    
    @Autowired
    public StepServiceImpl(ObjectProvider<ChainRegistry> chainRegistryProvider) {
        this.chainRegistryProvider = chainRegistryProvider;
    }

    @Override
    public Step getCurrentStep(StepContext context) {
        return context.getStep();
    }

    @Override
    public Step getNextStep(StepContext context) {
        StepGraph stepGraph = getStepGraph(context);
        return stepGraph != null ? stepGraph.getNextStep(null) : null;
    }

    @Override
    public void advanceToNextStep(StepContext context) {
        StepGraph stepGraph = getStepGraph(context);
        if (stepGraph != null) {
            stepGraph.advanceToNextStep(null);
        }
    }

    @Override
    public void resetSteps(StepContext context) {
        StepGraph stepGraph = getStepGraph(context);
        if (stepGraph != null) {
            stepGraph.reset();
        }
    }

    @Override
    public StepContext executeStep(StepContext context) {
        AtomicReference<StepContext> processed = new AtomicReference<>(context);
        chainRegistryProvider.ifAvailable(chainRegistry ->{
             processed.set((StepContext) chainRegistry.process(context));
        });
        return processed.get();
    }

    @Override
    public StepStatus getStepStatus(StepContext context) {
        return context.getStatus();
    }

    @Override
    public List<Step> getAllSteps(StepContext context) {
        StepGraph stepGraph = getStepGraph(context);
        return stepGraph != null ? stepGraph.getAllSteps() : List.of();
    }

    @Override
    public boolean isWorkflowComplete(StepContext context) {
        // Implement as needed, placeholder for now
        return false;
    }

    @Override
    public void setWorkflowComplete(StepContext context) {
        // Implement as needed
    }

    private StepGraph getStepGraph(StepContext context) {
        // You may need to adjust this depending on how StepGraph is accessed
        // For now, try to get from metadata or context
        Object graph = context.getMetadata().get("stepGraph");
        if (graph instanceof StepGraph) {
            return (StepGraph) graph;
        }
        return null;
    }
} 