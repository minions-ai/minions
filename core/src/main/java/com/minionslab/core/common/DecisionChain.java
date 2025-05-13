package com.minionslab.core.common;

public interface DecisionChain<T> {
    // --- Builder methods ---
    void addToStart(T decision);
    
    void addToEnd(T decision);
    
    void addBefore(T target, T decision);
    
    void addAfter(T target, T decision);
}
