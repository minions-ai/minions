package com.minionslab.core.step.impl;

import com.minionslab.core.common.message.Message;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SummarizeStep extends AbstractStep {
    private List<Message> sourceStepMessages;
    private String summaryTemplate;
    
    
}
