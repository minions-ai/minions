package com.minionslab.core.common.chain;

import java.util.List;

/**
 * ProcessContext represents the context or state passed through a chain of processors.
 * It acts as a carrier for results, metadata, and execution state, and can be extended
 * to support custom behaviors, result aggregation, or context snapshots.
 * <p>
 * Implementors can add additional fields, methods, or result handling logic as needed.
 *
 * @param <T> the type of ProcessResult held by this context
 */
public interface ProcessContext<T extends ProcessResult> {
    /**
     * Returns the list of results accumulated during processing.
     *
     * @return the list of results
     */
    List<T> getResults();
    
    default void addResult(T result) {
        this.getResults().add(result);
    }
    

    

} 