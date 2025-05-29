package com.minionslab.core.step.definition;

import com.minionslab.core.step.impl.SetEntityStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

@StepDefinitionType(type = "set_entity", description = "Step for setting entity values in the workflow.")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SetEntityStepDefinition extends AbstractStepDefintion<SetEntityStep> {
    private String entity;
    private Map<String, String> keyValueMap;

    @Override
    public SetEntityStep buildStep() {
        SetEntityStep step = new SetEntityStep();
        configureStep(step);
        step.setEntity(this.entity);
        step.setKeyValueMap(this.keyValueMap);
        return step;
    }
    
}