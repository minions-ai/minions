package com.minionslab.core.step.customizer;

import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.step.impl.ModelCallStep;

//This is an example of a customizer that customizes the default ModelCallStep


public class ModelCallStepCustomizer implements StepCustomizer<ModelCallStep> {
    @Override
    public void customize(ModelCallStep step) {
        step.setModelConfig(ModelConfig.builder()
                                       .modelId("gpt-4")
                                       .provider("openai")
                                       .version("2024-01-01")
                                       .maxContextLength(4096)
                                       .maxTokens(512)
                                       .temperature(0.7)
                                       .topP(1.0)
                                       .build()
                           );
        
    }
}
