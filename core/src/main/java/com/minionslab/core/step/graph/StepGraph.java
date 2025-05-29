package com.minionslab.core.step.graph;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.step.Step;

import java.util.List;

/**
 * StepGraph defines the structure and transitions of steps in a workflow.
 * <p>
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Implement this interface to define custom step graph structures, transitions, or execution logic.</li>
 *   <li>Override methods to add advanced branching, looping, or conditional transitions.</li>
 * </ul>
 * <b>Usage:</b> Use StepGraph to represent and manage the flow of steps in agent workflows. Extend for custom orchestration patterns.
 */
public interface StepGraph {
    Step getCurrentStep();
    Step getNextStep(AgentContext context);
    void reset();
    List<Step> getAllSteps();
    
    void addStep(Step step);
    
    void addTransition(Step from, Step to);
    
    void advanceToNextStep(AgentContext context);
    
    void complete();
}