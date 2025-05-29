package com.minionslab.core.common.chain;


public interface ChainCustomizer {
    void customize(Chain chain);
    
    public boolean accepts(Chain chain);
}