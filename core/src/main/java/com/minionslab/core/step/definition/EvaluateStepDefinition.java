package com.minionslab.core.step.definition;

import com.minionslab.core.step.impl.EvaluateStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@StepDefinitionType(type = "evaluate", description = "Step for evaluating criteria or conditions.")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class EvaluateStepDefinition extends AbstractStepDefintion<EvaluateStep> {
    private String criteria;
    private String targetStepId;
    private String promptTemplate;

    @Override
    public EvaluateStep buildStep() {
        EvaluateStep step = new EvaluateStep();
        configureStep(step);
        step.setCriteria(this.criteria);
        step.setTargetStepId(this.targetStepId);
        step.setPromptTemplate(this.promptTemplate);
        return step;
    }
} 