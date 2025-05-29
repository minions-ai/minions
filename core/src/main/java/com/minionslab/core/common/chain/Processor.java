package com.minionslab.core.common.chain;

import java.util.concurrent.CompletableFuture;

/**
 * Processor defines a unit of work in a chain of responsibility pattern.
 * Processors can be composed into chains, extended with custom hooks, and decorated
 * with customizers for advanced behaviors such as logging, metrics, or error handling.
 * <p>
 * Implementors can override hooks for before/after/error, and can use priority and description
 * for advanced selection or UI purposes.
 *
 * @param <T> the type of ProcessContext processed by this processor
 */
public interface Processor<T extends ProcessContext> {
    /**
     * Returns true if this processor can handle the given context.
     *
     * @param input the context to check
     * @return true if this processor accepts the context
     */
    boolean accepts(T input);
    
    /**
     * Asynchronously execute the logic for the given context.
     *
     * @param input the context to process
     * @return a CompletableFuture for the processed context
     */
    default CompletableFuture<T> processAsync(T input) {
        return CompletableFuture.completedFuture(process(input));
    }
    
    /**
     * Synchronously execute the logic for the given context.
     *
     * @param input the context to process
     * @return the processed context
     */
    T process(T input);
    
    /**
     * Optional: Priority for processor selection (higher is preferred).
     *
     * @return the priority value
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Optional: Description for debugging or UI.
     *
     * @return a human-readable description
     */
    default String getDescription() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Optional: Pre-execution hook. Can be overridden for setup or logging.
     *
     * @param input the context to process
     * @return the (possibly modified) context
     */
    default T beforeProcess(T input) {
        return input;
    }
    
    /**
     * Optional: Post-execution hook. Can be overridden for cleanup or logging.
     *
     * @param input the context to process
     * @return the (possibly modified) context
     */
    default T afterProcess(T input) {
        return input;
    }
    
    /**
     * Optional: Error handling hook. Can be overridden for custom error handling.
     *
     * @param input the context to process
     * @param e the exception thrown
     * @return the (possibly modified) context
     */
    default T onError(T input, Exception e) {
        return input;
    }
    
    
} 