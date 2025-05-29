package com.minionslab.core.step.definition;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@StepDefinitionType(type = "dummy", description = "Dummy step definition for testing.")
class DummyStepDefinition implements StepDefinition<DummyStepDefinition.DummyStep> {
    @Override
    public DummyStep buildStep() {
        return new DummyStep();
    }
    
    static class DummyStep implements com.minionslab.core.step.Step {
        @Override
        public void customize(com.minionslab.core.step.customizer.StepCustomizer customizer) {
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
        public com.minionslab.core.message.Message getSystemPrompt() {
            return null;
        }
        
        @Override
        public com.minionslab.core.message.Message getGoal() {
            return null;
        }
    }
}

class StepDefinitionRegistryTest {
    @Test
    void testRegistryRegistersAndFindsByType() {
        DummyStepDefinition dummy = new DummyStepDefinition();
        StepDefinitionRegistry registry = new StepDefinitionRegistry(List.of(dummy));
        Class<? extends StepDefinition<?>> clazz = registry.getByType("dummy");
        assertNotNull(clazz);
        assertEquals(DummyStepDefinition.class, clazz);
    }
    
    @Test
    void testRegistryReturnsNullForUnknownType() {
        DummyStepDefinition dummy = new DummyStepDefinition();
        StepDefinitionRegistry registry = new StepDefinitionRegistry(List.of(dummy));
        assertThrows(UnknownStepTypeException.class, () -> registry.getByType("unknown"));
    }
} 