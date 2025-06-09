package com.minionslab.core.step.definition;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.step.impl.AskUserStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ask_user")
public class AskUserStepDefinition extends AbstractStepDefintion<AskUserStep> {
    private String question;
    private String inputType;
    private boolean optional;
    private String promptTemplate;
    private Message systemPrompt;
    
    @Override
    public AskUserStep buildStep() {
        AskUserStep step = new AskUserStep();
        configureStep(step);
        step.setQuestion(this.question);
        step.setInputType(this.inputType);
        step.setOptional(this.optional);
        
        return step;
    }


    @Override
    public String getDescription() { return "Step for asking a question to the user."; }
} 