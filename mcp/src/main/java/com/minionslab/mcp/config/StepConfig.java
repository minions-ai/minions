package com.minionslab.mcp.config;

import com.minionslab.mcp.step.MCPStep;
import com.minionslab.mcp.step.StepInstructionsConverter;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.support.DefaultConversionService;

public class StepConfig {
    
    @Bean
    public StructuredOutputConverter<MCPStep.StepInstruction> stepInstructionOutputConverter() {
        return new StepInstructionsConverter(new DefaultConversionService());
    }
}
