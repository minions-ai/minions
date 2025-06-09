package com.minionslab.core.step.impl;

import com.minionslab.core.common.message.Message;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.customizer.StepCustomizer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractStep implements Step {
    private String id;
    private Message goal;
    private String promptTemplate;
    private Message systemPrompt;
    private String type;
    
    public void customize(StepCustomizer customizer) {
        if (customizer != null) {
            customizer.customize(this);
        }
    }
}
