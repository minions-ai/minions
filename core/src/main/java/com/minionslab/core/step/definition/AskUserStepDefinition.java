package com.minionslab.core.step.definition;

import com.minionslab.core.step.impl.AskUserStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@StepDefinitionType(type = "ask_user", description = "Step for asking a question to the user.")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AskUserStepDefinition extends AbstractStepDefintion<AskUserStep> {
    private String question;
    private String inputType;
    private boolean optional;

    @Override
    public AskUserStep buildStep() {
        AskUserStep step = new AskUserStep();
        configureStep(step);
        step.setQuestion(this.question);
        step.setInputType(this.inputType);
        step.setOptional(this.optional);
        return step;
    }
} 