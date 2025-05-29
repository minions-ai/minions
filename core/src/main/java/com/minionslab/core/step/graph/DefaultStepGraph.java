package com.minionslab.core.step.graph;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.step.Step;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultStepGraph implements StepGraph {
    
    private final Map<String, Step> stepById;
    private final Map<String, List<Step>> transitions;
    private final TransitionStrategy transitionStrategy;
    private final Step startStep;
    
    private Step currentStep;
    
    public DefaultStepGraph(StepGraphDefinition definition) {
        
        
        transitionStrategy = definition.getTransitionStrategy();
        this.stepById = definition.getSteps().stream()
                                  .collect(Collectors.toMap(Step::getId, s -> s));
        
        this.transitions = definition.getTransitions().entrySet().stream()
                                     .collect(Collectors.toMap(
                                             Map.Entry::getKey,
                                             e -> e.getValue().stream().map(stepById::get).toList()
                                                              ));
        
        this.startStep = currentStep = definition.getStartStep();
    }
    
    
    @Override
    public Step getCurrentStep() {
        return currentStep;
    }
    
    @Override
    public Step getNextStep(AgentContext context) {
        List<Step> nextSteps = transitions.getOrDefault(currentStep.getId(), List.of());
        Step next = transitionStrategy.selectNext(currentStep, nextSteps, context);
        this.currentStep = next;
        return next;
    }
    
    @Override
    public void reset() {
        currentStep = startStep;
    }
    
    @Override
    public List<Step> getAllSteps() {
        return stepById.values().stream().toList();
    }
    
    @Override
    public void addStep(Step step) {
        if (stepById.containsKey(step.getId())) {
            throw new IllegalArgumentException("Step already exists: " + step.getId());
        }
        stepById.put(step.getId(), step);
    }
    
    @Override
    public void addTransition(Step from, Step to) {
        transitions.computeIfAbsent(from.getId(), k -> new java.util.ArrayList<>()).add(to);
    }
    
    @Override
    public void advanceToNextStep(AgentContext context) {
    
    }
    
    @Override
    public void complete() {
    
    }
}