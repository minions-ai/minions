package com.minionslab.core.step.definition;

import com.minionslab.core.message.Message;
import com.minionslab.core.step.impl.SummarizeStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("summarize")
public class SummarizeStepDefinition extends AbstractStepDefintion<SummarizeStep> {
    private List<Message> sourceStepMessages;
    private String summaryTemplate;
    private String promptTemplate;
    private Message systemPrompt;
    
    @Override
    public SummarizeStep buildStep() {
        SummarizeStep step = new SummarizeStep();
        configureStep(step);
        step.setSourceStepMessages(this.sourceStepMessages);
        step.setSummaryTemplate(this.summaryTemplate);
        return step;
    }

    @Override
    public String getType() { return "summarize"; }
    @Override
    public String getDescription() { return "Step for summarizing a set of messages."; }
} 