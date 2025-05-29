package com.minionslab.core.step.definition;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import org.reflections.Reflections;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*Register a new type for new Step Definition classes to be used by the StepFactory */
//todo: this class and all the processes around creating a new step need to be documented properly
public class StepDefinitionTypeIdResolver implements TypeIdResolver {
    
    private static final Map<String, Class<? extends StepDefinition<?>>> registry = new ConcurrentHashMap<>();
    private JavaType baseType;
    
    public static void register(String type, Class<? extends StepDefinition<?>> clazz) {
        registry.put(type, clazz);
    }
    
    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
        // Use Reflections to find all implementations of StepDefinition
        Reflections reflections = new Reflections(baseType.getRawClass().getPackageName());
        Set<Class<?>> subTypes = reflections.getSubTypesOf((Class<Object>) baseType.getRawClass());
        for (Class<?> clazz : subTypes) {
            if (StepDefinition.class.isAssignableFrom(clazz)) {
                StepDefinitionType annotation = clazz.getAnnotation(StepDefinitionType.class);
                if (annotation != null) {
                    String type = annotation.type();
                    //noinspection unchecked
                    registry.put(type, (Class<? extends StepDefinition<?>>) clazz);
                }
            }
        }
    }
    
    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return idFromValue(value);
    }
    
    @Override
    public String idFromValue(Object value) {
        
        for (Iterator<Map.Entry<String, Class<? extends StepDefinition<?>>>> iterator = registry.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Class<? extends StepDefinition<?>>> entry = iterator.next();
            if (entry.getValue().equals(value.getClass())) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Unknown type: " + value.getClass());
    }
    
    @Override
    public String idFromBaseType() {
        return "";
    }
    
    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        Class<?> clazz = registry.get(id);
        if (clazz == null) {
            throw new IllegalArgumentException("Unknown type id: " + id);
        }
        return context.constructType(clazz);
    }
    
    @Override
    public String getDescForKnownTypeIds() {
        return "";
    }
    
    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.NAME;
    }
    

}
