package com.minionslab.core.common.chain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * ProcessResult represents the outcome of processing a context through a processor or chain.
 * It captures the processor ID, result status, error (if any), timing, and a list of sub-results.
 * <p>
 * This class is designed for extensibility: you can subclass to add custom fields, behaviors,
 * or aggregation logic. Use the static factory methods for common result types (success, skipped, failure).
 *
 * @param <T> the type of sub-results or payloads held by this result
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ProcessResult<T> {
    /**
     * The ID of the processor that produced this result.
     */
    private  String processorId;
    /**
     * Whether the processor handled the context.
     */
    private  boolean handled;
    /**
     * The list of sub-results or payloads.
     */
    private  List<T> results;
    /**
     * The error thrown during processing, if any.
     */
    private  Throwable error;
    /**
     * The time processing started.
     */
    private  Instant startedAt;
    /**
     * The time processing ended.
     */
    private  Instant endedAt;
    
    /**
     * Protected no-arg constructor for subclassing.
     */
    protected ProcessResult() {}
    
    /**
     * Creates a successful result with the given processor ID, results, and start time.
     *
     * @param processorId the processor ID
     * @param results the list of results
     * @param start the start time
     * @return a successful ProcessResult
     */
    public static <T> ProcessResult<T> success(String processorId, List<T> results, Instant start) {
        return new ProcessResult<>(processorId, true, results, null, start, Instant.now());
    }
    
    /**
     * Creates a skipped result for the given processor ID.
     *
     * @param processorId the processor ID
     * @return a skipped ProcessResult
     */
    public static <T> ProcessResult<T> skipped(String processorId) {
        return new ProcessResult<>(processorId, false, null, null, null, Instant.now());
    }
    
    /**
     * Creates a failed result with the given processor ID, error, and start time.
     *
     * @param processorId the processor ID
     * @param error the error thrown
     * @param start the start time
     * @return a failed ProcessResult
     */
    public static <T> ProcessResult<T> failure(String processorId, Throwable error, Instant start) {
        return new ProcessResult<>(processorId, true, null, error, start, Instant.now());
    }
    
    /**
     * Returns true if the result is successful (handled and no error).
     *
     * @return true if successful
     */
    public boolean isSuccess() {
        return handled && error == null;
    }
    
    /**
     * Returns true if the result is a failure (has an error).
     *
     * @return true if failed
     */
    public boolean isFailure() {
        return error != null;
    }
    
    /**
     * Returns the duration between start and end times.
     *
     * @return the duration of processing
     */
    public Duration getDuration() {
        return Duration.between(startedAt, endedAt);
    }
}
