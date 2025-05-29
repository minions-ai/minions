package com.minionslab.core.step.definition;

import com.minionslab.core.step.impl.PlannerStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@StepDefinitionType(type = "planner", description = "Step for planning and orchestrating workflow steps.")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class PlannerStepDefinition extends AbstractStepDefintion<PlannerStep> {
    private String constraints;
    private String plannerName;

    @Override
    public PlannerStep buildStep() {
        PlannerStep step = new PlannerStep();
        configureStep(step);
        step.setConstraints(this.constraints);
        step.setPlannerName(this.plannerName);
        return step;
    }
} 