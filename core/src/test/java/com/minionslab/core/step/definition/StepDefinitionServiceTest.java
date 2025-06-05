package com.minionslab.core.step.definition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minionslab.core.step.Step;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StepDefinitionService}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Generate step definition strings</li>
 *   <li>Get object node with annotation</li>
 *   <li>Create step (success/unknown type)</li>
 *   <li>Get step definition class</li>
 *   <li>Polymorphic serialization/deserialization</li>
 * </ul>
 * <p>
 * Setup: Mocks StepDefinitionRegistry and initializes StepDefinitionService.
 */
class StepDefinitionServiceTest {
    private StepDefinitionRegistry registry;
    private StepDefinitionService service;
    
    /**
     * Sets up the test environment before each test.
     * Mocks StepDefinitionRegistry and initializes StepDefinitionService.
     * Expected: StepDefinitionService is ready for use in each test.
     */
    @BeforeEach
    void setUp() {
        registry = mock(StepDefinitionRegistry.class);
        service = new StepDefinitionService(registry);
    }
    
    /**
     * Tests that generateStepDefinitionStrings returns correct JSON strings for step definitions.
     * Setup: Registry returns a list with AnnotatedDummyStepDef.
     * Expected: List contains correct type and description.
     */
    @Test
    void testGenerateStepDefinitionStrings() throws Exception {
        // Mock a StepDefinition class with annotation
        
        when(registry.getAllDefinitions())
                .thenReturn(List.of(AnnotatedDummyStepDef.class));
        
        List<String> result = service.generateStepDefinitionStrings();
        assertEquals(1, result.size());
        assertTrue(result.get(0).contains("dummy"));
        assertTrue(result.get(0).contains("A dummy step"));
    }
    
    /**
     * Tests that getObjectNodeWithAnnotation returns correct type and description.
     * Setup: Uses DummyStepDef class.
     * Expected: ObjectNode contains correct type and description.
     */
    @Test
    void testGetObjectNodeWithAnnotation() {

        // Use reflection to access private method
        ObjectNode node = serviceTestGetObjectNode(DummyStepDef.class);
        assertEquals("dummy", node.get("type").asText());
        assertEquals("A dummy step", node.get("description").asText());
    }
    
    // Helper to access private getObjectNode
    private ObjectNode serviceTestGetObjectNode(Class<?> clazz) {
        try {
            var method = StepDefinitionService.class.getDeclaredMethod("getObjectNode", Class.class);
            method.setAccessible(true);
            return (ObjectNode) method.invoke(service, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    

    
    /**
     * Tests that createStep creates a step successfully when type is known.
     * Setup: Registry returns DummyStepDef/AnnotatedDummyStepDef for type.
     * Expected: StepDefinition instance is created.
     */
    @Test
    void testCreateStepSuccess() throws Exception {
        when(registry.getByType("dummy")).thenReturn((Class) DummyStepDef.class);
        when(registry.getByType("annotate_dummy")).thenReturn((Class) AnnotatedDummyStepDef.class);
        
        ObjectMapper mapper = new ObjectMapper();
        StepDefinition value = new AnnotatedDummyStepDef ();
        String json = mapper.writeValueAsString(value);
        StepDefinition<?> def = service.createStep("annotate_dummy", json);
        assertNotNull(def);
        assertTrue(def instanceof AnnotatedDummyStepDef);
    }
    
    /**
     * Tests that createStep throws IllegalArgumentException when type is unknown.
     * Setup: Registry returns null for unknown type.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    void testCreateStepUnknownType() {
        when(registry.getByType("unknown")).thenReturn(null);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> service.createStep("unknown", "{}"));
        assertTrue(ex.getMessage().contains("Unknown step type"));
    }
    
    /**
     * Tests that getStepDefinitionClass returns the correct class for a given type.
     * Setup: Registry returns DummyStepDef for type.
     * Expected: Correct class is returned.
     */
    @Test
    void testGetStepDefinitionClass() {

        when(registry.getByType("dummy")).thenReturn((Class) DummyStepDef.class);
        assertEquals(DummyStepDef.class, service.getStepDefinitionClass("dummy"));
    }
    

    
    
    @JsonTypeName("dummy")

    public static class DummyStepDef implements StepDefinition<Step> {
        @Override
        public Step buildStep() {
            return null;
        }
        
        @Override
        public String getType() {
            return "dummy";
        }
        
        @Override
        public String getDescription() {
            return "A dummy step";
        }
    }
    
    @JsonTypeName("annotate_dummy")
    @Data
    @Accessors(chain = true)
    public static class AnnotatedDummyStepDef implements StepDefinition<Step> {
        public AnnotatedDummyStepDef() {
            this.dummyField = "dummyField";
        }
        @Override
        public Step buildStep() {
            return null;
        }
        @Override
        public String getType() {
            return "dummy";
        }
        
        @Override
        public String getDescription() {
            return "A dummy step";
        }
        
        
        @JsonProperty("dummy-field")
        private String dummyField;
        

    }
}