package com.minionslab.core.step.definition;

import com.fasterxml.jackson.annotation.JsonTypeName;
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

/**
 * Unit tests for {@link StepDefinitionDeserializer}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Deserializes known type correctly</li>
 *   <li>Throws on unknown type</li>
 * </ul>
 * <p>
 * Setup: Mocks StepDefinitionRegistry and uses a custom deserializer.
 */
class StepDefinitionDeserializerTest {
    private StepDefinitionRegistry registry;
    private ObjectMapper mapper;
    private StepDefinitionDeserializer deserializer;
    
    /**
     * Sets up the test environment before each test.
     * Mocks StepDefinitionRegistry and initializes StepDefinitionDeserializer.
     * Expected: Deserializer is ready for use in each test.
     */
    @BeforeEach
    void setUp() {
        registry = mock(StepDefinitionRegistry.class);
        mapper = new ObjectMapper();
        deserializer = new StepDefinitionDeserializer(registry);
    }
    
    /**
     * Tests that deserializing a known type returns the correct StepDefinition instance.
     * Setup: Registry returns DummyStepDefinition for 'dummy' type.
     * Expected: Deserialized object is DummyStepDefinition with correct value.
     */
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
    
    /**
     * Tests that deserializing an unknown type throws IllegalArgumentException.
     * Setup: Registry returns null for unknown type.
     * Expected: IllegalArgumentException is thrown.
     */
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
    
    @JsonTypeName("dummy")
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
        @Override
        public String getType() { return "dummy"; }
        @Override
        public String getDescription() { return "Dummy step definition"; }
    }
} 