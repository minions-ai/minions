package com.minionslab.core.common.chain;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Chain defines the contract for a flexible, extensible chain of responsibility pattern
 * for processing a sequence of {@link Processor} instances. Chains can be dynamically
 * constructed, extended, or customized, allowing for pluggable and composable processing pipelines.
 * <p>
 * Implementations may support customizers, dynamic processor registration, and advanced
 * strategies for processor selection and execution order.
 *
 * @param <T> the type of Processor in the chain
 * @param <R> the type of ProcessContext processed by the chain
 */
public interface Chain<T extends Processor, R extends ProcessContext> {
    /**
     * Adds a processor to the start of the chain.
     *
     * @param processor the processor to add
     * @return this chain instance for fluent API
     */
    Chain addToStart(T processor);

    /**
     * Adds a processor to the end of the chain.
     *
     * @param processor the processor to add
     * @return this chain instance for fluent API
     */
    Chain addToEnd(T processor);

    /**
     * Adds a processor before a target processor in the chain.
     *
     * @param target the processor to insert before
     * @param processor the processor to add
     * @return this chain instance for fluent API
     */
    Chain addBefore(T target, T processor);

    /**
     * Adds a processor after a target processor in the chain.
     *
     * @param target the processor to insert after
     * @param processor the processor to add
     * @return this chain instance for fluent API
     */
    Chain addAfter(T target, T processor);

    /**
     * Removes a processor from the chain.
     *
     * @param processor the processor to remove
     * @return this chain instance for fluent API
     */
    Chain remove(T processor);

    /**
     * Returns a copy of the current list of processors in the chain.
     *
     * @return the list of processors
     */
    List<T> getProcessors();

    /**
     * Processes the input through the chain, invoking each processor that accepts the input.
     * Implementors may override to provide custom flow, error handling, or result aggregation.
     *
     * @param input the context to process
     * @return the processed context
     */
    R process(R input);

    /**
     * Returns true if any processor in the chain accepts the given context.
     *
     * @param context the context to check
     * @return true if any processor accepts, false otherwise
     */
    boolean accepts(R context);

    /**
     * Processes the input asynchronously through the chain. Default implementation uses a thread pool.
     *
     * @param input the context to process
     * @return a CompletableFuture for the processed context
     */
    default java.util.concurrent.CompletableFuture<R> processAsync(R input) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> process(input));
    }
}