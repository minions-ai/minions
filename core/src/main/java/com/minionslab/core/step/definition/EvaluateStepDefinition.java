package com.minionslab.core.step.definition;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.step.impl.EvaluateStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("evaluate")
public class EvaluateStepDefinition extends AbstractStepDefintion<EvaluateStep> {
    private String criteria;
    private String targetStepId;
    private String promptTemplate;
    private Message systemPrompt;
    
    @Override
    public EvaluateStep buildStep() {
        EvaluateStep step = new EvaluateStep();
        configureStep(step);
        step.setCriteria(this.criteria);
        step.setTargetStepId(this.targetStepId);
        step.setPromptTemplate(this.promptTemplate);
        return step;
    }

    @Override
    public String getType() { return "evaluate"; }
    @Override
    public String getDescription() { return "Step for evaluating criteria or conditions."; }
} 