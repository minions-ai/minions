package com.minionslab.mcp.step;

/**
 * Defines criteria for determining when a step is complete and whether more calls are needed.
 */
public interface StepCompletionCriteria {
    /**
     * Determines if the step execution is complete.
     *
     * @param execution The current step execution
     * @return true if the step is complete, false otherwise
     */
    boolean isComplete(StepExecution execution);


} 