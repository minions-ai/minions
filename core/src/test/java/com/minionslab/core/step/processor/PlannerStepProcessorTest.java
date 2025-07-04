package com.minionslab.core.step.processor;

import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.common.message.MessageRole;
import com.minionslab.core.common.message.SimpleMessage;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.definition.StepDefinitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PlannerStepProcessor}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Accepts context with planner step type</li>
 * </ul>
 * <p>
 * Setup: Mocks StepContext and Step. Initializes PlannerStepProcessor.
 */
class PlannerStepProcessorTest {
    @Mock ListableBeanFactory beanFactory;
    @Mock ChainRegistry chainRegistry;
    @Mock StepDefinitionService stepDefinitionService;
    @Mock StepContext context;
    @Mock Step step;
    PlannerStepProcessor processor;

    /**
     * Sets up the test environment before each test.
     * Mocks dependencies and initializes PlannerStepProcessor.
     * Expected: PlannerStepProcessor is ready for use in each test.
     */
    @BeforeEach
    void setUp() {
        beanFactory = mock(ListableBeanFactory.class);
        chainRegistry = mock(ChainRegistry.class);
        stepDefinitionService = mock(StepDefinitionService.class);
        context = mock(StepContext.class);
        step = mock(Step.class);
        processor = new PlannerStepProcessor(stepDefinitionService);
    }

    /**
     * Tests that accepts returns true for planner step type and false otherwise.
     * Setup: StepContext returns step with type 'planner' or 'other'.
     * Expected: accepts returns true for 'planner', false otherwise.
     */
    @Test
    void testAcceptsPlannerType() {
        when(context.getStep()).thenReturn(step);
        when(step.getType()).thenReturn("planner");
        assertTrue(processor.accepts(context));
        when(step.getType()).thenReturn("other");
        assertFalse(processor.accepts(context));
    }

    @Test
    void testProcessAddsModelCall() throws Exception {
        when(context.getStep()).thenReturn(step);
        SimpleMessage message = mock(SimpleMessage.class);
        when(message.getRole()).thenReturn(MessageRole.SYSTEM);
        when(step.getSystemPrompt()).thenReturn(message);
        when(stepDefinitionService.generateStepDefinitionStrings()).thenReturn(List.of("schema1", "schema2"));
        doNothing().when(context).addModelCall(any(ModelCall.class));
        StepContext result = processor.process(context);
        assertSame(context, result);
        verify(context).addModelCall(any(ModelCall.class));
    }
} 