package com.minionslab.core.step.impl;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlannerStep extends AbstractStep {
    private String constraints;
    private String plannerName;
    

}
