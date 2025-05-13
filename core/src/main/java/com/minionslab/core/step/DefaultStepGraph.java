package com.minionslab.core.step;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.step.decision.DefaultStepDecisionChain;
import com.minionslab.core.tool.ToolCallExecutorFactory;

import java.util.*;

public class DefaultStepGraph implements StepGraph {
    final List<Step> steps;
    private final Map<String, List<String>> stepGraph;
    private final DefaultStepDecisionChain nextStepDecisionChain;
    
    public DefaultStepGraph(List<Step> steps, Map<String, List<String>> stepGraphMap) {
        this(steps, stepGraphMap, new DefaultStepDecisionChain());
    }
    
    public DefaultStepGraph(List<Step> steps, Map<String, List<String>> stepGraphMap, DefaultStepDecisionChain nextStepDecisionChain) {
        this.steps = new ArrayList<>(steps);
        this.stepGraph = stepGraphMap != null ? new HashMap<>(stepGraphMap) : new HashMap<>();
        this.nextStepDecisionChain = nextStepDecisionChain;
    }
    
    
    @Override
    public List<Step> getPossibleNextSteps(Step currentStep) {
        if (currentStep == null)
            return Collections.emptyList();
        List<String> nextIds = stepGraph.getOrDefault(currentStep.getId(), List.of());
        List<Step> possible = new ArrayList<>();
        for (Step step : steps) {
            if (nextIds.contains(step.getId())) {
                possible.add(step);
            }
        }
        return possible;
    }
    
    @Override
    public Step selectNextStep(Step currentStep, List<Step> possibleNextSteps, AgentContext context, ToolCallExecutorFactory toolCallExecutorFactory) {
        return nextStepDecisionChain.decide(currentStep, possibleNextSteps, context, toolCallExecutorFactory);
    }
    
    @Override
    public List<Step> getSteps() {
        return Collections.unmodifiableList(steps);
    }
    
    @Override
    public void addStep(Step step) {
        if (step == null)
            throw new IllegalArgumentException("Step cannot be null");
        steps.add(step);
    }
} 