package com.minionslab.core.step.impl;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EvaluateStep extends AbstractStep {
    private String criteria;
    private String targetStepId;
    private String promptTemplate;
    
    @Override
    public String getType() {
        return "evaluate";
    }
}
