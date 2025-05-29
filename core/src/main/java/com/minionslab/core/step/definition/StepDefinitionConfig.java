package com.minionslab.core.step.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StepDefinitionConfig {
    
    @Bean
    public com.fasterxml.jackson.databind.Module stepDefinitionModule(StepDefinitionRegistry registry, ObjectMapper objectMapper) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(StepDefinition.class, new StepDefinitionDeserializer(registry, objectMapper));
        return module;
    }
    
}
