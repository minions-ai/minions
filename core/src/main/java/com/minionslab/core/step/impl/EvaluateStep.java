package com.minionslab.core.step.impl;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors
public class EvaluateStep extends AbstractStep {
    private String criteria;
    private String targetStepId;

    

}
