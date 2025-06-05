package com.minionslab.core.agent;

import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.memory.query.QueryConfig;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AgentConfig {
    private ModelConfig modelConfig;
    private QueryConfig queryConfig;
    
    
}
