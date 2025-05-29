package com.minionslab.core.common.chain;

import com.minionslab.core.step.processor.StepCompletionProcessor;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * AbstractBaseChain provides a flexible, extensible implementation of the Chain of Responsibility pattern
 * for processing a sequence of {@link Processor} instances. Processors can be dynamically added, removed,
 * or customized using {@link ProcessorCustomizer}s, allowing for highly configurable and pluggable chains.
 * <p>
 * Subclasses should implement {@link #registerProcessors()} to define the initial set of processors in the chain.
 * Customizers can be injected to modify or decorate processors before they are added to the chain, enabling
 * advanced behaviors such as logging, metrics, or conditional logic.
 * <p>
 * This class is designed for extension: you can create custom chains by subclassing and registering your own
 * processors and customizers, or by overriding the process flow.
 *
 * @param <T> the type of Processor in the chain
 * @param <R> the type of ProcessContext processed by the chain
 */
public abstract class AbstractBaseChain<T extends Processor, R extends ProcessContext> implements Chain<T, R> {
    /**
     * The list of processors in the chain, in execution order.
     */
    protected final List<T> processors = new ArrayList<>();
    /**
     * The list of customizers to apply to processors before adding them to the chain.
     */
    protected final List<ProcessorCustomizer> customizers = new ArrayList<>();
    
    /**
     * Constructs a new AbstractBaseChain with optional providers for processors and customizers.
     * Subclasses can use these providers to inject dependencies or discover available processors.
     *
     * @param processorProviders provider for the initial list of processors
     * @param customizerProviders provider for the initial list of processor customizers
     */
    public AbstractBaseChain(ObjectProvider<List<T>> processorProviders, ObjectProvider<List<ProcessorCustomizer>> customizerProviders) {
        processorProviders.ifAvailable(processorList -> this.processors.addAll(processorList));
        customizerProviders.ifAvailable(customizerList -> this.customizers.addAll(customizerList));
        registerProcessors();
    }
    
    /**
     * Subclasses must implement this method to register the initial set of processors in the chain.
     * This is the main extension point for defining custom processing logic.
     */
    protected abstract void registerProcessors();
    
    /**
     * Adds a processor to the start of the chain, applying all customizers first.
     *
     * @param processor the processor to add
     * @return this chain instance for fluent API
     */
    @Override
    public Chain addToStart(T processor) {
        doCustomize(processor);
        this.processors.add(0, processor);
        return this;
    }
    
    /**
     * Applies all registered customizers to the given processor if they accept it.
     * This allows for dynamic decoration or modification of processors.
     *
     * @param processor the processor to customize
     */
    private void doCustomize(T processor) {
        if (customizers != null) {
            customizers.stream().filter(customizer -> customizer.accepts(processor)).forEach(customizer -> customizer.customize(processor));
        }
    }
    
    /**
     * Adds a processor to the end of the chain, applying all customizers first.
     *
     * @param processor the processor to add
     * @return this chain instance for fluent API
     */
    @Override
    public Chain addToEnd(T processor) {
        doCustomize(processor);
        this.processors.add(processor);
        return this;
    }
    
    /**
     * Adds a processor before a target processor in the chain, applying all customizers first.
     * If the target is not found, adds to the start.
     *
     * @param target the processor to insert before
     * @param processor the processor to add
     * @return this chain instance for fluent API
     */
    @Override
    public Chain addBefore(T target, T processor) {
        doCustomize(processor);
        int idx = processors.indexOf(target);
        if (idx >= 0) {
            processors.add(idx, processor);
        } else {
            processors.add(0, processor);
        }
        return this;
    }
    
    /**
     * Adds a processor after a target processor in the chain, applying all customizers first.
     * If the target is not found or is the last, adds to the end.
     *
     * @param target the processor to insert after
     * @param processor the processor to add
     * @return this chain instance for fluent API
     */
    @Override
    public Chain addAfter(T target, T processor) {
        doCustomize(processor);
        int idx = processors.indexOf(target);
        if (idx >= 0 && idx < processors.size() - 1) {
            processors.add(idx + 1, processor);
        } else {
            processors.add(processor);
        }
        return this;
    }
    
    /**
     * Removes a processor from the chain.
     *
     * @param processor the processor to remove
     * @return this chain instance for fluent API
     */
    @Override
    public Chain remove(T processor) {
        processors.remove(processor);
        return this;
    }
    
    /**
     * Returns a copy of the current list of processors in the chain.
     *
     * @return the list of processors
     */
    @Override
    public List<T> getProcessors() {
        return new ArrayList<>(processors);
    }
    
    /**
     * Processes the input through the chain, invoking each processor that accepts the input.
     * Hooks for before/after/error are called on each processor. Subclasses can override for custom flow.
     *
     * @param input the context to process
     * @return the processed context
     */
    @Override
    public R process(R input) {
        for (T processor : processors) {
            if (!processor.accepts(input))
                continue;
            try {
                processor.beforeProcess(input);
                processor.process(input);
                processor.afterProcess(input);
            } catch (Exception e) {
                processor.onError(input, e);
            }
        }
        return input;
    }
    
    /**
     * Returns true if any processor in the chain accepts the given context.
     *
     * @param context the context to check
     * @return true if any processor accepts, false otherwise
     */
    @Override
    public boolean accepts(R context) {
        boolean accepted = false;
        for(T processor: processors){
            accepted = accepted || processor.accepts(context);
        }
        return accepted;
    }
}