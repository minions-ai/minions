package com.minionslab.core.step.graph;

import com.minionslab.core.step.Step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultStepGraphDefinition implements StepGraphDefinition {
    
    private final List<Step> steps = new ArrayList<>();
    private final Map<String, List<String>> transitions = new HashMap<>();
    private Step startStep;
    private TransitionStrategy transitionStrategy = new NextStepTransitionStrategy();
    
    public void addTransition(Step from, Step to) {
        transitions.computeIfAbsent(from.getId(), k -> new ArrayList<>()).add(to.getId());
    }
    
    @Override
    public Step getStartStep() {
        return startStep;
    }
    
    public void setStartStep(Step step) {
        this.startStep = step;
        addStep(step);
    }
    
    public void addStep(Step step) {
        steps.add(step);
    }
    
    @Override
    public List<Step> getSteps() {
        return steps;
    }
    
    @Override
    public Map<String, List<String>> getTransitions() {
        return transitions;
    }
    
    @Override
    public TransitionStrategy getTransitionStrategy() {
        return transitionStrategy;
    }
    
}
