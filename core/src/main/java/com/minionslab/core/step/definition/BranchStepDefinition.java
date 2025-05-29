package com.minionslab.core.step.definition;

import com.minionslab.core.step.impl.BranchStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@StepDefinitionType(type = "branch", description = "Step for branching workflow execution based on a condition.")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class BranchStepDefinition extends AbstractStepDefintion<BranchStep> {
    private String conditionExpr;
    private List<StepDefinition<?>> thenSteps;
    private List<StepDefinition<?>> elseSteps;

    @Override
    public BranchStep buildStep() {
        BranchStep step = new BranchStep();
        configureStep(step);
        step.setConditionExpr(this.conditionExpr);
        step.setThenSteps(this.thenSteps);
        step.setElseSteps(this.elseSteps);
        return step;
    }
} 