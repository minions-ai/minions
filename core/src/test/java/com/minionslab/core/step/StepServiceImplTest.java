package com.minionslab.core.step;

import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.step.graph.StepGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StepServiceImplTest {
    @Mock
    private ObjectProvider<ChainRegistry> chainRegistryProvider;
    @Mock
    private ChainRegistry chainRegistry;
    @Mock
    private StepGraph stepGraph;
    @Mock
    private Step step;
    private StepContext context;
    private StepServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new StepServiceImpl(chainRegistryProvider);
        context = mock(StepContext.class);
        when(context.getStep()).thenReturn(step);
        when(context.getStatus()).thenReturn(StepStatus.PENDING);
        Map<String, Object> metadata = new HashMap<>();
        when(context.getMetadata()).thenReturn(metadata);
    }

    @Test
    void testGetCurrentStep() {
        assertEquals(step, service.getCurrentStep(context));
    }

    @Test
    void testGetNextStep() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("stepGraph", stepGraph);
        when(context.getMetadata()).thenReturn(metadata);
        when(stepGraph.getNextStep(null)).thenReturn(step);
        assertEquals(step, service.getNextStep(context));
    }

    @Test
    void testAdvanceToNextStep() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("stepGraph", stepGraph);
        when(context.getMetadata()).thenReturn(metadata);
        doNothing().when(stepGraph).advanceToNextStep(null);
        service.advanceToNextStep(context);
        verify(stepGraph).advanceToNextStep(null);
    }

    @Test
    void testResetSteps() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("stepGraph", stepGraph);
        when(context.getMetadata()).thenReturn(metadata);
        doNothing().when(stepGraph).reset();
        service.resetSteps(context);
        verify(stepGraph).reset();
    }

    @Test
    void testExecuteStep() {
        doAnswer(invocation -> {
            ((java.util.function.Consumer<ChainRegistry>) invocation.getArgument(0)).accept(chainRegistry);
            return null;
        }).when(chainRegistryProvider).ifAvailable(any());
        when(chainRegistry.process(context)).thenReturn(context);
        StepContext result = service.executeStep(context);
        assertEquals(context, result);
        verify(chainRegistry).process(context);
    }

    @Test
    void testGetStepStatus() {
        assertEquals(StepStatus.PENDING, service.getStepStatus(context));
    }

    @Test
    void testGetAllSteps() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("stepGraph", stepGraph);
        when(context.getMetadata()).thenReturn(metadata);
        List<Step> steps = List.of(step);
        when(stepGraph.getAllSteps()).thenReturn(steps);
        assertEquals(steps, service.getAllSteps(context));
    }

    @Test
    void testGetAllStepsReturnsEmptyListIfNoGraph() {
        when(context.getMetadata()).thenReturn(new HashMap<>());
        assertTrue(service.getAllSteps(context).isEmpty());
    }

    @Test
    void testIsWorkflowComplete() {
        assertFalse(service.isWorkflowComplete(context));
    }

    @Test
    void testSetWorkflowComplete() {
        // No-op, just ensure no exception
        service.setWorkflowComplete(context);
    }
} 