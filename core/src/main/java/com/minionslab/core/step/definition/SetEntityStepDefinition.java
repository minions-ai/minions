package com.minionslab.core.step.definition;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.step.impl.SetEntityStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("set_entity")
public class SetEntityStepDefinition extends AbstractStepDefintion<SetEntityStep> {
    private String entity;
    private Map<String, String> keyValueMap;
    private String promptTemplate;
    private Message systemPrompt;
    
    @Override
    public SetEntityStep buildStep() {
        SetEntityStep step = new SetEntityStep();
        configureStep(step);
        step.setEntity(this.entity);
        step.setKeyValueMap(this.keyValueMap);
        return step;
    }

    @Override
    public String getType() { return "set_entity"; }
    @Override
    public String getDescription() { return "Step for setting entity values in the workflow."; }
}