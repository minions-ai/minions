package com.minionslab.core.step.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.StepException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link StepDefinitionService}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Generate step definition strings (integration)</li>
 *   <li>Create step and get class (integration)</li>
 *   <li>Throws on unknown type (integration)</li>
 * </ul>
 * <p>
 * Setup: Initializes StepDefinitionRegistry and StepDefinitionService with IntegrationStepDef.
 */
public class StepDefinitionServiceIntegrationTest {
    private StepDefinitionRegistry registry;
    private StepDefinitionService service;

    public static class DummyStep implements Step {
        @Override
        public void customize(com.minionslab.core.step.customizer.StepCustomizer customizer) {}
        @Override
        public String getId() { return "dummy-id"; }
        @Override
        public String getType() { return "integration"; }
        @Override
        public com.minionslab.core.message.Message getSystemPrompt() { return null; }
        @Override
        public com.minionslab.core.message.Message getGoal() { return null; }
    }

    @StepDefinitionType(type = "integration", description = "Integration test step")
    public static class IntegrationStepDef implements StepDefinition<DummyStep> {
        @Override
        public DummyStep buildStep() { return new DummyStep(); }
        @Override
        public String getType() { return "integration"; }
        @Override
        public String getDescription() { return "Integration test step"; }
    }

    @BeforeEach
    void setUp() {
        registry = new StepDefinitionRegistry(List.of(new IntegrationStepDef()));
        service = new StepDefinitionService(registry);
    }

    /**
     * Tests that generateStepDefinitionStrings returns correct JSON strings for integration step definitions.
     * Setup: Registry contains IntegrationStepDef.
     * Expected: List contains correct type and description.
     */
    @Test
    void testGenerateStepDefinitionStrings() throws Exception {
        List<String> result = service.generateStepDefinitionStrings();
        assertEquals(1, result.size());
        assertTrue(result.get(0).contains("integration"));
        assertTrue(result.get(0).contains("Integration test step"));
    }

    /**
     * Tests that createStep and getStepDefinitionClass work for integration step definitions.
     * Setup: Registry contains IntegrationStepDef, JSON for IntegrationStepDef.
     * Expected: StepDefinition instance is created and correct class is returned.
     */
    @Test
    void testCreateStepAndGetStepDefinitionClass() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IntegrationStepDef def = new IntegrationStepDef();
        String json = mapper.writeValueAsString(def);
        StepDefinition<?> created = service.createStep("integration", json);
        assertNotNull(created);
        assertTrue(created instanceof IntegrationStepDef);
        assertEquals(IntegrationStepDef.class, service.getStepDefinitionClass("integration"));
    }

    /**
     * Tests that createStep throws UnknownStepTypeException for unknown type (integration).
     * Setup: Registry does not contain the type.
     * Expected: UnknownStepTypeException is thrown.
     */
    @Test
    void testCreateStepUnknownType() {
        Exception ex = assertThrows(StepException.UnknownStepTypeException.class, () -> service.createStep("unknown", "{}"));
        assertTrue(ex.getMessage().contains("Unknown step type"));
    }
} 