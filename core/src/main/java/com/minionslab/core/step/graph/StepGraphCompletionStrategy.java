package com.minionslab.core.step.graph;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.step.Step;

/**
 * StepGraphCompletionStrategy defines the policy for determining when a step graph (workflow) is complete.
 * <p>
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Implement this interface to define custom completion logic for workflows.</li>
 *   <li>Plug in different strategies for different agent or workflow types.</li>
 * </ul>
 * <b>Usage:</b> Use StepGraphCompletionStrategy to control workflow termination and completion policies.
 */
public interface StepGraphCompletionStrategy {
    boolean isComplete(StepGraph graph, Step currentStep, AgentContext context);
}