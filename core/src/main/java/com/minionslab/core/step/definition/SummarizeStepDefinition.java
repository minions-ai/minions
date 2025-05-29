package com.minionslab.core.step.definition;

import com.minionslab.core.message.Message;
import com.minionslab.core.step.impl.SummarizeStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@StepDefinitionType(type = "summarize", description = "Step for summarizing a set of messages.")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SummarizeStepDefinition extends AbstractStepDefintion<SummarizeStep> {
    private List<Message> sourceStepMessages;
    private String summaryTemplate;

    @Override
    public SummarizeStep buildStep() {
        SummarizeStep step = new SummarizeStep();
        configureStep(step);
        step.setSourceStepMessages(this.sourceStepMessages);
        step.setSummaryTemplate(this.summaryTemplate);
        return step;
    }
} 