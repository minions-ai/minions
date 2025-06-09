package com.minionslab.core.common.chain;


public interface ChainCustomizer {
    void customize(Chain chain);
    
    boolean accepts(Chain chain);
}