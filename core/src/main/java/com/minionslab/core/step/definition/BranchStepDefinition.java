package com.minionslab.core.step.definition;

import com.minionslab.core.message.Message;
import com.minionslab.core.step.impl.BranchStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("branch")
public class BranchStepDefinition extends AbstractStepDefintion<BranchStep> {
    private String conditionExpr;
    private List<StepDefinition<?>> thenSteps;
    private List<StepDefinition<?>> elseSteps;
    private String promptTemplate;
    private Message systemPrompt;
    
    @Override
    public BranchStep buildStep() {
        BranchStep step = new BranchStep();
        configureStep(step);
        step.setConditionExpr(this.conditionExpr);
        step.setThenSteps(this.thenSteps);
        step.setElseSteps(this.elseSteps);
        return step;
    }

    @Override
    public String getType() { return "branch"; }
    @Override
    public String getDescription() { return "Step for branching workflow execution based on a condition."; }
} 