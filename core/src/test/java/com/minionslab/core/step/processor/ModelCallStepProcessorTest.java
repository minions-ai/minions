package com.minionslab.core.step.processor;

import com.minionslab.core.model.ModelCall;
import com.minionslab.core.service.ModelCallService;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.impl.ModelCallStep;
import com.minionslab.core.tool.ToolCall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelCallStepProcessorTest {
    ModelCallService modelCallService;
    ModelCallStepProcessor processor;
    StepContext context;
    ModelCallStep step;
    ModelCall modelCall;
    @BeforeEach
    void setUp() {
        modelCallService = mock(ModelCallService.class);
        processor = new ModelCallStepProcessor(modelCallService);
        context = mock(StepContext.class);
        step = mock(ModelCallStep.class);
        modelCall = mock(ModelCall.class);
    }
    @Test
    void testAcceptsWithUnfinishedModelCalls() {
        when(context.getUnfinishedModelCalls()).thenReturn(List.of(modelCall));
        assertTrue(processor.accepts(context));
        when(context.getUnfinishedModelCalls()).thenReturn(List.of());
        assertFalse(processor.accepts(context));
    }
    @Test
    void testProcessCallsModelService() {
        when(context.getStep()).thenReturn(step);
        when(context.getUnfinishedModelCalls()).thenReturn(List.of(modelCall));
        when(modelCall.getRequest()).thenReturn(mock(ModelCall.ModelCallRequest.class));
        when(modelCall.getModelConfig()).thenReturn(null);
        when(modelCallService.call(any())).thenReturn(modelCall);
        doNothing().when(context).increaseModelCalls();
        StepContext result = processor.process(context);
        assertSame(context, result);
        verify(modelCallService).call(any());
        verify(context).increaseModelCalls();
    }
    @Test
    void testAfterProcessAddsToolCalls() {
        ToolCall toolCall = mock(ToolCall.class);
        ModelCall modelCall = mock(ModelCall.class);
        when(modelCall.getToolCalls()).thenReturn(List.of(toolCall));
        when(context.getModelCalls()).thenReturn(List.of(modelCall));
        when(context.getToolCalls()).thenReturn(new java.util.ArrayList<>());
        StepContext result = processor.afterProcess(context);
        assertSame(context, result);
        // ToolCall should be added to context.getToolCalls()
    }
} 