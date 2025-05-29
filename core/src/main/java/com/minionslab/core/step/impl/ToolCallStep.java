package com.minionslab.core.step.impl;

import com.minionslab.core.message.Message;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.customizer.StepCustomizer;

import java.util.Map;

public class ToolCallStep implements Step {
    private final String id;
    private final Message goal;
    private final String toolName;
    private final Map<String, Object> input;
    private final Map<String, Object> params;
    private final String outputVar;
    
    private StepContext stepContext;
    
    public ToolCallStep(String id, Message goal, String toolName, Map<String, Object> input,
                        Map<String, Object> params, String outputVar) {
        this.id = id;
        this.goal = goal;
        this.toolName = toolName;
        this.input = input;
        this.params = params;
        this.outputVar = outputVar;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public Map<String, Object> getInput() {
        return input;
    }
    
    public Map<String, Object> getParams() {
        return params;
    }
    
    public String getOutputVar() {
        return outputVar;
    }
    
    @Override
    public void customize(StepCustomizer customizer) {
    
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public Message getGoal() {
        return goal;
    }
    

    
    @Override
    public Message getSystemPrompt() {
        return null;
    }
    
    @Override
    public String getType() {
        return "tool_call";
    }
}