package com.minionslab.core.step.definition;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.step.impl.PlannerStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("planner")
public class PlannerStepDefinition extends AbstractStepDefintion<PlannerStep> {
    private String constraints;
    private String plannerName;
    private String promptTemplate;
    private Message systemPrompt;
    
    @Override
    public PlannerStep buildStep() {
        PlannerStep step = new PlannerStep();
        configureStep(step);
        step.setConstraints(this.constraints);
        step.setPlannerName(this.plannerName);
        step.setPromptTemplate(promptTemplate);
        step.setSystemPrompt(systemPrompt);
        return step;
    }
    
    @Override
    public String getType() {
        return "planner";
    }
    
    @Override
    public String getDescription() {
        return "Step for planning and orchestrating workflow steps.";
    }
} 