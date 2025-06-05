package com.minionslab.core.step.definition;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StepDefinitionConfig {
    
    @Bean
    public com.fasterxml.jackson.databind.Module stepDefinitionModule(StepDefinitionRegistry registry) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(StepDefinition.class, new StepDefinitionDeserializer(registry));
        return module;
    }
    
    

    
    
}
