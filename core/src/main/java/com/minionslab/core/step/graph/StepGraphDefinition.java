package com.minionslab.core.step.graph;

import com.minionslab.core.step.Step;

import java.util.List;
import java.util.Map;

public interface StepGraphDefinition {
    Step getStartStep();
    
    List<Step> getSteps();
    
    Map<String, List<String>> getTransitions();
    
    TransitionStrategy getTransitionStrategy();
}
