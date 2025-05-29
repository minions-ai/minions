package com.minionslab.core.agent;

import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.graph.StepGraph;
import com.minionslab.core.memory.MemoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgentProcessorTest {
    private ChainRegistry chainRegistry;
    private AgentProcessor processor;
    private AgentContext context;
    private Agent agent;
    private AgentRecipe recipe;
    private StepGraph stepGraph;
    private Step step;
    private StepContext stepContext;
    private MemoryManager memoryManager;

    @BeforeEach
    void setUp() {
        chainRegistry = mock(ChainRegistry.class);
        processor = new AgentProcessor(chainRegistry);
        context = mock(AgentContext.class);
        agent = mock(Agent.class);
        recipe = mock(AgentRecipe.class);
        stepGraph = mock(StepGraph.class);
        step = mock(Step.class);
        stepContext = mock(StepContext.class);
        memoryManager = mock(MemoryManager.class);
        when(context.getAgent()).thenReturn(agent);
        when(context.getRecipe()).thenReturn(recipe);
        when(context.getMemoryManager()).thenReturn(memoryManager);
        when(recipe.getStepGraph()).thenReturn(stepGraph);
    }

    @Test
    void testBeforeProcessCallsSnapshot() {
        AgentContext result = processor.beforeProcess(context);
        verify(memoryManager).snapshot();
        assertEquals(context, result);
    }

    @Test
    void testAfterProcessCallsFlush() {
        AgentContext result = processor.afterProcess(context);
        verify(memoryManager).flush();
        assertEquals(context, result);
    }

    @Test
    void testOnErrorCallsRestoreLatestSnapshot() {
        Exception e = new RuntimeException("fail");
        AgentContext result = processor.onError(context, e);
        verify(memoryManager).restoreLatestSnapshot();
        assertEquals(context, result);
    }

    @Test
    void testAccepts() {
        assertTrue(processor.accepts(context));
        assertFalse(processor.accepts(null));
    }

    @Test
    void testProcessIteratesStepsAndCallsChainRegistry() {
        when(stepGraph.getNextStep(context)).thenReturn(step).thenReturn(null);
        when(chainRegistry.process(any(StepContext.class))).thenReturn(stepContext);
        AgentContext result = processor.process(context);
        verify(chainRegistry).process(any(StepContext.class));
        assertEquals(context, result);
    }
} 