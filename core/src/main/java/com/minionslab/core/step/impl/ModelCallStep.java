package com.minionslab.core.step.impl;

import com.minionslab.core.config.ModelConfig;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ModelCallStep extends AbstractStep {
    
    private String promptTemplate;
    private List<String> inputVars;
    private Map<String, Object> params;
    private String outputVar;
    private ModelConfig modelConfig;
    
    
    @Override
    public String getType() {
        return "";
    }
}
