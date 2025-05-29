package com.minionslab.core.config;

import com.minionslab.core.common.chain.ChainCustomizer;
import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.common.chain.ProcessorCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DeafultChainConfiguration {
    
    private final ChainRegistry chainRegistry;
    
    private final ObjectProvider<List<ProcessorCustomizer>> processorCustomizers;
    
    public DeafultChainConfiguration(ChainRegistry chainRegistry, ObjectProvider<List<ChainCustomizer>> chainCustomizers,
                                     ObjectProvider<List<ProcessorCustomizer>> processorCustomizers) {
        this.chainRegistry = chainRegistry;
        
        this.processorCustomizers = processorCustomizers;
    }
    
    
} 