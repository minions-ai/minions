package com.minionslab.core.service;

import com.minionslab.core.common.chain.Chain;
import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.common.chain.ProcessorCustomizer;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.model.ModelInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service

/*
How to customize processors:
If you need to customize the processors, implement @ProcessorCustomizer as a Spring Component or a bean and make sure it accepts the type of processor that you want to
customize
* */
public class ModelCallService {
    
    private final Map<String, ModelInfo> modelInfoCache = new ConcurrentHashMap<>();
    

    private List<AIModelProvider> modelCallProviders = new ArrayList<>();
    
    public ModelCallService(ObjectProvider<List<AIModelProvider>> providers) {
        providers.ifAvailable(aIproviders-> this.modelCallProviders.addAll(aIproviders));
    }
    
    public Set<String> getModelNames() {
        return modelInfoCache.keySet();
    }
    
    public void clearCache() {
        modelInfoCache.clear();
    }
    
    public int getCacheSize() {
        return modelInfoCache.size();
    }
    
    public ModelCall call(ModelCall modelCall) {
        modelCallProviders.stream().filter(provider -> provider.accepts(modelCall)).findFirst().ifPresent(provider -> {provider.process(modelCall);});
        return modelCall;
    }
    
    
    /**
     * Retrieves ModelInfo by provider and modelId (case-insensitive).
     */
    public ModelInfo getModelInfo(String provider, String modelId) {
        return modelInfoCache.values().stream()
                             .filter(info -> info.provider().equalsIgnoreCase(provider)
                                                     && info.modelId().equalsIgnoreCase(modelId))
                             .findFirst()
                             .orElse(null);
    }
}
