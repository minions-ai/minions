package com.minionslab.mcp.config;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;


@Configuration
public class StepCompletionToolConfig {
    
    
    @Bean
    public void getStepCompletionTool(CompletionRecord record){
        Method stepCompleted = ReflectionUtils.findMethod(this.getClass(), "stepCompleted", CompletionRecord.class);
        MethodToolCallback.builder()
                .toolDefinition(getToolDefinition())
                .toolMethod(stepCompleted)
                .build();
    }
    
    public record CompletionRecord() {
    }
    
    
    private ToolDefinition getToolDefinition(){
        ToolDefinition toolDefinition = new ToolDefinition() {
            
            
            @Override
            public String name() {
                return "step_completed";
            }
            
            @Override
            public String description() {
                return "Signals that the current step is complete. Carries result, error, and status information.";
            }
            
            
            @Override
            public String inputSchema() {
                Map<String, Map<String, ? extends Serializable>> schemaMap = Map.of(
                        "stepId", Map.of("type", "string", "description", "The unique identifier of the step being completed.", "required", true),
                        "result", Map.of("type", "string", "description", "The result, answer, or summary for the step.", "required", false),
                        "cannotFinish", Map.of("type", "boolean", "description", "Indicates the LLM/agent cannot finish the step.", "required", false),
                        "unrecoverableError", Map.of("type", "boolean", "description", "Indicates an unrecoverable error occurred.", "required", false),
                        "reason", Map.of("type", "string", "description", "Explanation if the step cannot be finished or an error occurred.", "required", false),
                        "timestamp", Map.of("type", "string", "description", "When the step was marked as completed (ISO 8601).", "required", false),
                        "metadata", Map.of("type", "object", "description", "Additional structured data for extensibility.", "required", false),
                        "confidence", Map.of("type", "number", "description", "Confidence score for the result.", "required", false),
                        "nextStepSuggestion", Map.of("type", "string", "description", "Suggestion for what to do next if the step cannot be finished.", "required", false)
                                                                                   
                                                                                   );
                ObjectMapper mapper = new ObjectMapper();
                
                try {
                    String s = mapper.writeValueAsString(schemaMap);
                    
                    return s;
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                
            }
            
        };
        
        return toolDefinition;
    }
}
