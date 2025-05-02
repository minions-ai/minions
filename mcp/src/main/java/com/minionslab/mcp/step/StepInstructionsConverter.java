package com.minionslab.mcp.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.mcp.step.MCPStep;
import org.springframework.ai.converter.AbstractConversionServiceOutputConverter;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.Map;

public class StepInstructionsConverter extends AbstractConversionServiceOutputConverter<MCPStep.StepInstruction> {
    
    private final String jsonSchema;
    
    public StepInstructionsConverter(DefaultConversionService conversionService) {
        super(conversionService);
        this.jsonSchema = buildJsonSchema();
    }
    
    private String buildJsonSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "stepId": { "type": "string", "description": "The unique identifier of the step being completed." },
                "result": { "type": "string", "description": "The result, answer, or summary for the step." },
                "cannotFinish": { "type": "boolean", "description": "Indicates the LLM/agent cannot finish the step." },
                "unrecoverableError": { "type": "boolean", "description": "Indicates an unrecoverable error occurred." },
                "reason": { "type": "string", "description": "Explanation if the step cannot be finished or an error occurred." },
                "timestamp": { "type": "string", "description": "When the step was marked as completed (ISO 8601)." },
                "metadata": { "type": "object", "description": "Additional structured data for extensibility." },
                "confidence": { "type": "number", "description": "Confidence score for the result." },
                "nextStepSuggestion": { "type": "string", "description": "Suggestion for what to do next if the step cannot be finished." }
              },
              "required": ["stepId"]
            }
            """;
    }
    
    @Override
    public String getFormat() {
        return String.format("""
            Your response should be in JSON format.
            Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.
            Do not include markdown code blocks in your response.
            Here is the JSON Schema instance your output must adhere to:
            %s
            """, this.jsonSchema);
    }
    
    @Override
    public MCPStep.StepInstruction convert(String source) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = objectMapper.readValue(source, Map.class);
            
            return new MCPStep.StepInstruction(
                    (String) map.get("stepId"),
                    (String) map.get("result"),
                    (Boolean) map.get("cannotFinish"),
                    (Boolean) map.get("unrecoverableError"),
                    (String) map.get("reason"),
                    (String) map.get("timestamp"),
                    (Map<String, Object>) map.get("metadata"),
                    map.get("confidence") != null ? ((Number) map.get("confidence")).doubleValue() : null,
                    (String) map.get("nextStepSuggestion")
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert source to StepInstruction: " + source, e);
        }
    }
}
