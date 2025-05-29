package com.minionslab.core.common.chain;

public interface ChainDefinition {
    String getName();
    Chain build(); // Defines the base/default chain
}
