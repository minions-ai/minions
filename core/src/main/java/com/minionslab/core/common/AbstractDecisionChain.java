package com.minionslab.core.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractDecisionChain<T> implements DecisionChain<T> {
    protected final List<T> chain = new ArrayList<>();

    @Override
    public void addToStart(T decision) {
        chain.add(0, decision);
    }

    @Override
    public void addToEnd(T decision) {
        chain.add(decision);
    }

    @Override
    public void addBefore(T target, T decision) {
        int idx = chain.indexOf(target);
        if (idx >= 0) {
            chain.add(idx, decision);
        } else {
            chain.add(0, decision);
        }
    }

    @Override
    public void addAfter(T target, T decision) {
        int idx = chain.indexOf(target);
        if (idx >= 0 && idx < chain.size() - 1) {
            chain.add(idx + 1, decision);
        } else {
            chain.add(decision);
        }
    }
    
    public void customize(Consumer<List<T>> customizer) {
        customizer.accept(chain);
    }
    

} 