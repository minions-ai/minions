package com.minionslab.core.step.definition;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.step.impl.ToolCallStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("tool_call")
public class ToolCallStepDefinition extends AbstractStepDefintion<ToolCallStep> {
    private String toolName;
    private Map<String, Object> input;
    private Map<String, Object> params;
    private String outputVar;
    private String promptTemplate;
    private Message systemPrompt;
    
    @Override
    public ToolCallStep buildStep() {
        ToolCallStep step = new ToolCallStep(
            this.getId(),
            this.getGoal(),
            this.toolName,
            this.input,
            this.params,
            this.outputVar
        );
        return step;
    }

    @Override
    public String getType() { return "tool_call"; }
    @Override
    public String getDescription() { return "Step for making a tool call with parameters."; }
} 