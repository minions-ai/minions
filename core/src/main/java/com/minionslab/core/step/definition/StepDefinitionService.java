package com.minionslab.core.step.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StepDefinitionService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StepDefinitionRegistry registry;
    

    
    @Autowired
    public StepDefinitionService(StepDefinitionRegistry registry) {
        this.registry = registry;
    }
    

    
    public List<String> generateStepDefinitionStrings() throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        
        List<Class<? extends StepDefinition<?>>> allDefinitions = registry.getAllDefinitions();
        
        List<String> schemas = new java.util.ArrayList<>();
        for (Class<?> clazz : allDefinitions) {
            ObjectNode objectNode = getObjectNode(clazz);
            String s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
            schemas.add(s);
        }
        return schemas;
    }
    
    private ObjectNode getObjectNode(Class<?> clazz) {
        ObjectNode node = objectMapper.createObjectNode();
        try {
            StepDefinition<?> instance = (StepDefinition<?>) clazz.getDeclaredConstructor().newInstance();
            node.put("className", clazz.getName());
            node.put("type", instance.getType());
            node.put("description", instance.getDescription());
        } catch (Exception e) {
            // Could not instantiate, skip type/description
            log.error("Could not instantiate class: {}", clazz.getName(), e);
        }
        return node;
    }
    

    
    public StepDefinition createStep(String type, String json) throws Exception {
        Class<? extends StepDefinition> defClass = this.getStepDefinitionClass(type);
        
        if (defClass == null) {
            throw new IllegalArgumentException("Unknown step type: " + type);
        }
        StepDefinition<?> def = objectMapper.readValue(json, defClass);
        return def;
    }
    
    public Class<? extends StepDefinition> getStepDefinitionClass(String type) {
        return registry.getByType(type);
    }
}