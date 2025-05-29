package com.minionslab.core.memory.strategy;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.minionslab.core.memory.MemoryOperation;
import java.util.EnumMap;

@Slf4j(topic = "MemoryStrategy")
@Component
public class MemoryStrategyRegistry {
    private final Map<String, MemoryStrategy> nameToStrategy = new ConcurrentHashMap<>();
    private final Map<Class<? extends MemoryStrategy>, List<MemoryStrategy>> typeToStrategies = new ConcurrentHashMap<>();
    private final Map<MemoryOperation, MemoryStrategy> defaultStrategies = new EnumMap<>(MemoryOperation.class);
    private final Map<MemoryOperation, MemoryStrategy> noOpStrategies = new EnumMap<>(MemoryOperation.class);
    
    public MemoryStrategyRegistry(ObjectProvider<List<MemoryStrategy>> strategiesProvider) {
        List<MemoryStrategy> strategies = strategiesProvider.getIfAvailable(Collections::emptyList);
        if (strategies != null) {
            for (MemoryStrategy strategy : strategies) {
                register(strategy);
            }
        }
    }

    public void register(MemoryStrategy strategy) {
        if (strategy == null) return;
        nameToStrategy.put(strategy.getName(), strategy);
        // Register for all interfaces in the hierarchy
        for (Class<?> iface : getAllStrategyTypes(strategy.getClass())) {
            if (MemoryStrategy.class.isAssignableFrom(iface)) {
                typeToStrategies.computeIfAbsent((Class<? extends MemoryStrategy>) iface, k -> new ArrayList<>()).add(strategy);
            }
        }
    }

    public MemoryStrategy getByName(String name) {
        return nameToStrategy.get(name);
    }

    public <T extends MemoryStrategy> T getByType(Class<T> type) {
        List<MemoryStrategy> list = typeToStrategies.get(type);
        if (list != null && !list.isEmpty()) {
            return type.cast(list.get(0));
        }
        return null;
    }

    public <T extends MemoryStrategy> T getDefaultByType(Class<T> type) {
        return getByType(type);
    }

    public Collection<MemoryStrategy> getAllStrategies() {
        return Collections.unmodifiableCollection(nameToStrategy.values());
    }

    private Set<Class<?>> getAllStrategyTypes(Class<?> clazz) {
        Set<Class<?>> types = new HashSet<>();
        while (clazz != null && clazz != Object.class) {
            for (Class<?> iface : clazz.getInterfaces()) {
                if (MemoryStrategy.class.isAssignableFrom(iface)) {
                    types.add(iface);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return types;
    }

    /**
     * Configure a default strategy for a specific MemoryOperation.
     */
    public void setDefaultForOperation(MemoryOperation op, MemoryStrategy strategy) {
        defaultStrategies.put(op, strategy);
    }

    /**
     * Get the default strategy for a MemoryOperation. If not configured, return a NoOpStrategy.
     */
    public MemoryStrategy getDefaultForOperation(MemoryOperation op) {
        MemoryStrategy strategy = defaultStrategies.get(op);
        if (strategy != null) return strategy;
        // If not configured, create or return a cached NoOpStrategy
        return noOpStrategies.computeIfAbsent(op, NoOpStrategy::new);
    }
    
}
