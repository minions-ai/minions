package com.minionslab.core.step.impl;

import com.minionslab.core.step.definition.StepDefinition;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class BranchStep extends AbstractStep {
    private String conditionExpr;
    private List<StepDefinition<?>> thenSteps;
    private List<StepDefinition<?>> elseSteps;
    
    @Override
    public String getType() {
        return "branch";
    }
}
