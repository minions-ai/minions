package com.minionslab.core.tool;

import java.util.Map;

public class DefaultToolDefinition implements ToolDefinition {
    @Override
    public String getName() {
        return "";
    }
    
    @Override
    public String getDescription() {
        return "";
    }
    
    @Override
    public Map<String, Object> getParameters() {
        return Map.of();
    }
    
    @Override
    public boolean isEnabled() {
        return false;
    }
    
    @Override
    public String getVersion() {
        return "";
    }
}
