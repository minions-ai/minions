package com.minionslab.core.step.definition;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StepDefinitionRegistry {
    
    private final Map<String, Class<? extends StepDefinition<?>>> typeMap = new HashMap<>();
    
    public StepDefinitionRegistry(List<StepDefinition<?>> beans) {
        for (StepDefinition<?> step : beans) {
            StepDefinitionType meta = step.getClass().getAnnotation(StepDefinitionType.class);
            if (meta != null) {
                typeMap.put(meta.type(), (Class<? extends StepDefinition<?>>) step.getClass());
            }
        }
    }
    
    public Class<? extends StepDefinition<?>> getByType(String type) {
        Class<? extends StepDefinition<?>> clazz = typeMap.get(type);
        if (clazz == null) {
            throw new UnknownStepTypeException("Unknown step type: " + type);
        }
        return clazz;
    }
}
