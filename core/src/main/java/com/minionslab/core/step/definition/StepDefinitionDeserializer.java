package com.minionslab.core.step.definition;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class StepDefinitionDeserializer extends JsonDeserializer<StepDefinition<?>> {
    
    private final StepDefinitionRegistry registry;
    private final ObjectMapper mapper;
    
    
    public StepDefinitionDeserializer(StepDefinitionRegistry registry, ObjectMapper mapper) {
        this.registry = registry;
        this.mapper = mapper;
    }
    
    @Override
    public StepDefinition<?> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        
        JsonNode node = p.getCodec().readTree(p);
        JsonNode type1 = node.get("type");
        if (type1 == null) {
            throw new IllegalArgumentException("Missing type field in step definition");
        }
        String type = type1.asText();
        
        Class<? extends StepDefinition<?>> clazz = registry.getByType(type);
        if (clazz == null) {
            throw new IllegalArgumentException("Unknown step type: " + type);
        }
        
        return mapper.treeToValue(node, clazz);
    }
}
