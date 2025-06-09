package com.minionslab.core.agent;

import com.minionslab.core.config.ModelConfig;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SummarizationConfig {
    private int inputMessageLimit;
    private ModelConfig modelConfig;
    

}
