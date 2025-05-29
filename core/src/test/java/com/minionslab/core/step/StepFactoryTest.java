package com.minionslab.core.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.minionslab.core.message.Message;
import com.minionslab.core.step.customizer.StepCustomizer;
import com.minionslab.core.step.definition.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StepFactoryTest {
    private StepDefinitionRegistry registry = new StepDefinitionRegistry(List.of(new DummyStepDefinition()));
    private StepFactory stepFactory;
    
    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(StepDefinition.class, new StepDefinitionDeserializer(registry, objectMapper));
        objectMapper.registerModule(module);
        stepFactory = new StepFactory(objectMapper);
    }
    
    @Test
    void testRegisterAndCreateStep() throws Exception {
        
        String json = "{\"value\":\"test\",\"type\":\"dummy\"}";
        Step step = stepFactory.createStep(json);
        assertNotNull(step);
        assertTrue(step instanceof DummyStep);
    }
    
    @Test
    void testUnknownStepTypeThrows() {
        String type = "notSoDummy";
        Exception ex = assertThrows(UnknownStepTypeException.class, () ->
                                                                            stepFactory.createStep("{\"value\":\"test\",\"type\":\"" + type + "\"}")
                                   );
        assertTrue(ex.getMessage().contains("Unknown step type: " + type));
    }
    
    @Test
    void testMissingTypeThrows() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                                                                            stepFactory.createStep("{}")
                                   );
        assertTrue(ex.getMessage().contains("Missing type field in step definition"));
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
    
    @StepDefinitionType(type = "dummy", description = "Dummy step definition")
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