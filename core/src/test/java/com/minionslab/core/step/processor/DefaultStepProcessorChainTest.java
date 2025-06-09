package com.minionslab.core.step.processor;

import com.minionslab.core.common.chain.ProcessorCustomizer;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.StepService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultStepProcessorChainTest {
    @Mock
    private ObjectProvider<List<StepProcessor>> processorProviders;
    @Mock
    private ObjectProvider<List<ProcessorCustomizer>> customizerProviders;
    @Mock
    private StepService stepService;
    @Mock
    private PlannerStepProcessor plannerStepProcessor;
    @Mock
    private ModelCallStepProcessor modelCallStepProcessor;
    @Mock
    private ToolCallStepProcessor toolCallStepProcessor;
    @Mock
    private StepCompletionProcessor stepCompletionProcessor;
    @Mock
    private PreparationProcessor preparationProcessor;

    private DefaultStepProcessorChain chain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(processorProviders.getIfAvailable()).thenReturn(List.of(plannerStepProcessor, modelCallStepProcessor, toolCallStepProcessor,stepCompletionProcessor,preparationProcessor));
        when(customizerProviders.getIfAvailable(any())).thenReturn(List.of());
        chain = new DefaultStepProcessorChain(
                processorProviders,
                customizerProviders,
                stepService
        );
    }

    @Test
    void testRegisterProcessorsOrder() {
        // The processors list should be in the order: preparation, planner, model, tool, completion
        chain.registerProcessors();
        List<StepProcessor> processors = chain.getProcessors();
        assertEquals(5, processors.size());
        assertSame(preparationProcessor, processors.get(0));
        assertSame(plannerStepProcessor, processors.get(1));
        assertSame(modelCallStepProcessor, processors.get(2));
        assertSame(toolCallStepProcessor, processors.get(3));
        assertSame(stepCompletionProcessor, processors.get(4));
    }

    @Test
    void testAccepts() {
        StepContext stepContext = mock(StepContext.class);
        assertTrue(chain.accepts(stepContext));
        // Negative test: a ProcessContext that is not a StepContext
        com.minionslab.core.common.chain.ProcessContext<?> otherContext = mock(com.minionslab.core.common.chain.ProcessContext.class);
        assertFalse(chain.accepts(otherContext));
    }
    
    
    @Test
    void testProcessStepContext(){
        //setup
        StepContext stepContext = mock(StepContext.class);
        when(preparationProcessor.accepts(any())).thenReturn(true);
        when(plannerStepProcessor.accepts(any())).thenReturn(true);
        when(modelCallStepProcessor.accepts(any())).thenReturn(true);
        when(toolCallStepProcessor.accepts(any())).thenReturn(true);
        StepContext process = chain.process(stepContext);
        assertNotNull(process);
        verify(preparationProcessor).process(stepContext);
        verify(plannerStepProcessor).process(stepContext);
        verify(modelCallStepProcessor).process(stepContext);
        verify(toolCallStepProcessor).process(stepContext);

    }
} 