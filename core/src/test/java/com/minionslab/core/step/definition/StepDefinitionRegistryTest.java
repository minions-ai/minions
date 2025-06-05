package com.minionslab.core.step.definition;

import com.minionslab.core.step.StepException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StepDefinitionRegistry}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Register and find step definition by type</li>
 *   <li>Throws exception on unknown type</li>
 * </ul>
 * <p>
 * Setup: Creates a registry with a DummyStepDefinition.
 */
class StepDefinitionRegistryTest {
    /**
     * Tests that the registry can register and find a step definition by type.
     * Setup: Registry contains DummyStepDefinition.
     * Expected: getByType returns DummyStepDefinition class.
     */
    @Test
    void testRegistryRegistersAndFindsByType() {
        DummyStepDefinition dummy = new DummyStepDefinition();
        StepDefinitionRegistry registry = new StepDefinitionRegistry(List.of(dummy));
        Class<? extends StepDefinition<?>> clazz = registry.getByType("dummy");
        assertNotNull(clazz);
        assertEquals(DummyStepDefinition.class, clazz);
    }
    
    /**
     * Tests that the registry throws UnknownStepTypeException for an unknown type.
     * Setup: Registry contains DummyStepDefinition.
     * Expected: getByType throws UnknownStepTypeException for unknown type.
     */
    @Test
    void testRegistryReturnsNullForUnknownType() {
        DummyStepDefinition dummy = new DummyStepDefinition();
        StepDefinitionRegistry registry = new StepDefinitionRegistry(List.of(dummy));
        assertThrows(StepException.UnknownStepTypeException.class, () -> registry.getByType("unknown"));
    }
} 