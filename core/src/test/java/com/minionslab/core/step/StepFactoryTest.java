package com.minionslab.core.step;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionslab.core.message.Message;
import com.minionslab.core.step.customizer.StepCustomizer;
import com.minionslab.core.step.definition.StepDefinition;
import com.minionslab.core.step.definition.StepDefinitionRegistry;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link StepFactory}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Deserialization of valid JSON with type</li>
 *   <li>Exception on unknown type</li>
 *   <li>Exception on missing type</li>
 * </ul>
 * <p>
 * Setup: Initializes a StepFactory with a registry containing a DummyStepDefinition and a default ObjectMapper.
 */
@ExtendWith(MockitoExtension.class)
class StepFactoryTest {
    private StepDefinitionRegistry registry = new StepDefinitionRegistry(List.of(new DummyStepDefinition()));
    private StepFactory stepFactory;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private ObjectMapper objectMapper;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private AutowireCapableBeanFactory beanFactory;
    
    /**
     * Sets up the test environment before each test.
     * Mocks dependencies and initializes the StepFactory.
     * Expected: StepFactory is ready for use in each test.
     */
    @BeforeEach
    void setUp() {
        stepFactory = new StepFactory(objectMapper);
    }
    
    /**
     * Tests that a valid JSON with a known type is deserialized into the correct Step instance.
     * Setup: JSON with type 'dummy' and value.
     * Expected: Step instance of DummyStep is returned.
     */
    @Test
    void testRegisterAndCreateStep() throws Exception {
        ObjectMapper realMapper = new ObjectMapper();
        realMapper.registerSubtypes(StepFactoryTest.DummyStepDefinition.class);
        AutowireCapableBeanFactory mockBeanFactory = mock(AutowireCapableBeanFactory.class);
        StepFactory realFactory = new StepFactory(realMapper);
        String json = "{\"value\":\"test\",\"type\":\"dummy\"}";
        Step step = realFactory.createStep(json);
        assertNotNull(step);
        assertTrue(step instanceof DummyStep);
    }
    
    /**
     * Tests that an UnknownStepTypeException is thrown when an unknown type is provided in the JSON.
     * Setup: JSON with unknown type.
     * Expected: UnknownStepTypeException is thrown.
     */
    @Test
    void testUnknownStepTypeThrows() {
        String type = "notSoDummy";
        Exception ex = assertThrows(StepException.class, () ->
                                                                 stepFactory.createStep("{\"value\":\"test\",\"type\":\"" + type + "\"}")
                                   );
        assertTrue(ex.getMessage().contains("StepDefinition is null"));
    }
    
    /**
     * Tests that an IllegalArgumentException is thrown when the type property is missing from the JSON.
     * Setup: JSON without type property.
     * Expected: IllegalArgumentException is thrown.
     */
    @Test
    void testMissingTypeThrows() {
        Exception ex = assertThrows(StepException.class, () ->
                                                                 stepFactory.createStep("{}")
                                   );
        assertTrue(ex.getMessage().contains("StepDefinition is null"));
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
    @Data
    @Accessors(chain = true)
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
        public String getType() {
            return "dummy";
        }
        
        @Override
        public String getDescription() {
            return "Dummy step definition";
        }
    }
} 