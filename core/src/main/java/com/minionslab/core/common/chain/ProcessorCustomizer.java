package com.minionslab.core.common.chain;

public interface ProcessorCustomizer<T extends Processor> {
    void customize(T processor);
    
    boolean accepts(T processor);
}
