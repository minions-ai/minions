package com.minionslab.core.step.definition;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.message.Message;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.customizer.StepCustomizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class StepDefinitionDeserializerTest {
    private StepDefinitionRegistry registry;
    private ObjectMapper mapper;
    private StepDefinitionDeserializer deserializer;
    
    @BeforeEach
    void setUp() {
        registry = mock(StepDefinitionRegistry.class);
        mapper = new ObjectMapper();
        deserializer = new StepDefinitionDeserializer(registry, mapper);
    }
    
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testDeserializeKnownType() throws IOException {
        String json = "{\"type\":\"dummy\",\"value\":\"test\"}";
        
        // Use raw type to avoid Mockito generic issues
        when(registry.getByType(eq("dummy"))).thenReturn((Class) DummyStepDefinition.class);
        JsonParser parser = mapper.getFactory().createParser(json);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        StepDefinition<?> result = deserializer.deserialize(parser, ctxt);
        assertTrue(result instanceof DummyStepDefinition);
        assertEquals("test", ((DummyStepDefinition) result).value);
    }
    
    @Test
    void testDeserializeUnknownTypeThrows() throws IOException {
        String json = "{\"type\":\"unknown\"}";
        when(registry.getByType("unknown")).thenReturn(null);
        JsonParser parser = mapper.getFactory().createParser(json);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertThrows(IllegalArgumentException.class, () -> deserializer.deserialize(parser, ctxt));
    }
    
    static class DummyStep implements Step {
        @Override
        public void customize(StepCustomizer customizer) {
        }
        
        @Override
        public String getId() {
            return "dummy-id";
        }
        
        @Override
        public String getType() {
            return "dummy";
        }
        
        @Override
        public Message getSystemPrompt() {
            return null;
        }
        
        @Override
        public Message getGoal() {
            return null;
        }
    }
    
    @StepDefinitionType(type = "dummy",description = "Dummy step definition")
    static class DummyStepDefinition implements StepDefinition<DummyStep> {
        public String type;
        public String value;
        
        public DummyStepDefinition() {
        }
        
        public DummyStepDefinition(String value) {
            this.value = value;
        }
        
        public DummyStep buildStep() {
            return new DummyStep();
        }
    }
} 