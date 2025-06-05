package com.minionslab.core.step.definition;

import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.message.Message;
import com.minionslab.core.step.impl.ModelCallStep;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@JsonTypeName("model_call")
@Data
@Accessors
public class ModelCallStepDefinition extends AbstractStepDefintion<ModelCallStep> {
    private ModelConfig modelConfig;
    private List<String> inputVars;
    private Map<String, Object> params;
    private String promptTemplate;


    private Message systemPrompt;
    
    @Override
    public ModelCallStep buildStep() {
        ModelCallStep step = new ModelCallStep();
        step.setModelConfig(this.modelConfig);
        step.setInputVars(this.inputVars);
        step.setParams(this.params);
        step.setPromptTemplate(this.promptTemplate);
        step.setGoal(this.getGoal());
        step.setId(this.getId());
        step.setSystemPrompt(this.getSystemPrompt());
        return step;
    }
    
    @Override
    public String getDescription() { return "Step for making a model call with a prompt and parameters."; }
    

}
