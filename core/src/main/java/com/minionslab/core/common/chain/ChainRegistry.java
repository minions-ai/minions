package com.minionslab.core.common.chain;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChainRegistry {
    
    private final ObjectProvider<List<ChainCustomizer>> chainCustomizerProviders;
    private final ObjectProvider<List<ProcessorCustomizer>> processorCustomizerProviders;
    private final Map<String, Chain> chains = new ConcurrentHashMap<>();
    
    @Autowired
    public ChainRegistry(ObjectProvider<List<ChainCustomizer>> chainCustomizerProviders, ObjectProvider<List<ProcessorCustomizer>> processorCustomizerProviders, Map<String, Chain> chains) {
        this.chainCustomizerProviders = chainCustomizerProviders;
        this.processorCustomizerProviders = processorCustomizerProviders;
        chains.forEach(this::register);
    }
    
    public void register(String chainName, Chain chain) {
        chainCustomizerProviders.ifAvailable(customizers -> customizers.stream().filter(c -> c.accepts(chain)).forEach(c -> c.customize(chain)));
        chains.put(chainName, chain);
    }
    
    
    public void unregisterChain(Chain chain) {
        chains.entrySet().removeIf(c -> c.getValue() == chain);
    }
    
    
    public void unregisterChain(String chainName) {
        chains.remove(chainName);
    }
    
    
    public boolean canProcess(ProcessContext context) {
        return chains.values().stream().anyMatch(chain -> chain.accepts(context));
    }
    
    
    //todo this method picks the first chain that accepts the context. Investigate whether we need to iterate over all chains.
    
    public ProcessContext process(ProcessContext context) {
        for (Chain chain : chains.values()) {
            if (chain.accepts(context)) {
                return chain.process(context);
            }
        }
        throw new IllegalArgumentException("No chain found for context: " + (context == null ? "null" : context.getClass().getSimpleName()));
    }
    
    public Chain<Processor, ProcessContext> getChain(String chainName) {
        return chains.get(chainName);
    }
}