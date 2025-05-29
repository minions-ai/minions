package com.minionslab.core.step.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
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

@Service
public class StepDefinitionService implements ApplicationContextAware {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ListableBeanFactory beanFactory;
    private ConfigurableApplicationContext configCtx;
    private Map<String, Class<? extends StepDefinition>> annotatedBeans = new HashMap<>();
    
    @Autowired
    public StepDefinitionService(ListableBeanFactory beanFactory, ConfigurableApplicationContext configCtx) {
        this.beanFactory = beanFactory;
        this.configCtx = configCtx;
    }
    
    public Map<String, Class<? extends StepDefinition>> getStepDefinitionMap() {
        return annotatedBeans;
    }
    
    public List<String> generateStepDefinitionStrings() throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        
        // Use reflection to find all classes annotated with @StepDefinitionType
        
        List<String> schemas = new java.util.ArrayList<>();
        for (Class<?> clazz : annotatedBeans.values()) {
            ObjectNode objectNode = getObjectNode(clazz);
            String s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
            schemas.add(s);
        }
        return schemas;
    }
    
    private ObjectNode getObjectNode(Class<?> clazz) {
        StepDefinitionType annotation = clazz.getAnnotation(StepDefinitionType.class);
        ObjectNode node = objectMapper.createObjectNode();
        if (annotation != null) {
            node.put("className", clazz.getName());
            node.put("type", annotation.type());
            node.put("description", annotation.description());
            
        }
        return node;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (!(applicationContext instanceof ConfigurableApplicationContext))
            return;
        this.configCtx = (ConfigurableApplicationContext) applicationContext;
        String[] beanNames = beanFactory.getBeanNamesForAnnotation(StepDefinitionType.class);
        
        for (String beanName : beanNames) {
            BeanDefinition def = configCtx.getBeanFactory().getBeanDefinition(beanName);
            String className = def.getBeanClassName();
            if (className == null)
                continue;
            
            try {
                Class<?> clazz = Class.forName(className);
                StepDefinitionType annotation = clazz.getAnnotation(StepDefinitionType.class);
                if (annotation != null && StepDefinition.class.isAssignableFrom(clazz)) {
                    annotatedBeans.put(annotation.type(), (Class<? extends StepDefinition>) clazz);
                }
            } catch (ClassNotFoundException e) {
                // handle or log if needed
            }
        }
        
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
        return annotatedBeans.get(type);
    }
}