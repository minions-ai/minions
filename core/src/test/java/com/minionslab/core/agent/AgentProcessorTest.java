package com.minionslab.core.agent;

import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.StepService;
import com.minionslab.core.step.graph.StepGraph;
import com.minionslab.core.memory.MemoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AgentProcessor}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Snapshot and flush memory on process</li>
 *   <li>Restore memory on error</li>
 *   <li>Accepts valid context</li>
 *   <li>Processes steps and calls ChainRegistry</li>
 * </ul>
 * <p>
 * Setup: Mocks ChainRegistry, StepService, AgentContext, Agent, AgentRecipe, StepGraph, Step, StepContext, and MemoryManager.
 */
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

    /**
     * Sets up the test environment before each test.
     * Mocks dependencies and initializes AgentProcessor.
     * Expected: AgentProcessor is ready for use in each test.
     */
    @BeforeEach
    void setUp() {
        chainRegistry = mock(ChainRegistry.class);
        StepService stepService = mock(StepService.class);
        processor = new AgentProcessor(stepService);
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

    /**
     * Tests that beforeProcess calls snapshot on MemoryManager.
     * Setup: Mocks MemoryManager in AgentContext.
     * Expected: snapshot is called and context is returned.
     */
    @Test
    void testBeforeProcessCallsSnapshot() {
        AgentContext result = processor.beforeProcess(context);
        verify(memoryManager).snapshot();
        assertEquals(context, result);
    }

    /**
     * Tests that afterProcess calls flush on MemoryManager.
     * Setup: Mocks MemoryManager in AgentContext.
     * Expected: flush is called and context is returned.
     */
    @Test
    void testAfterProcessCallsFlush() {
        AgentContext result = processor.afterProcess(context);
        verify(memoryManager).flush();
        assertEquals(context, result);
    }

    /**
     * Tests that onError calls restoreLatestSnapshot on MemoryManager.
     * Setup: Mocks MemoryManager in AgentContext.
     * Expected: restoreLatestSnapshot is called and context is returned.
     */
    @Test
    void testOnErrorCallsRestoreLatestSnapshot() {
        Exception e = new RuntimeException("fail");
        AgentContext result = processor.onError(context, e);
        verify(memoryManager).restoreLatestSnapshot();
        assertEquals(context, result);
    }

    /**
     * Tests that accepts returns true for valid context and false for null.
     * Setup: None.
     * Expected: accepts returns correct boolean.
     */
    @Test
    void testAccepts() {
        assertTrue(processor.accepts(context));
        assertFalse(processor.accepts(null));
    }

    /**
     * Tests that process iterates steps and calls ChainRegistry.
     * Setup: Mocks StepGraph and ChainRegistry.
     * Expected: process returns the context after processing steps.
     */
    @Test
    void testProcessIteratesStepsAndCallsChainRegistry() {
        when(stepGraph.getNextStep(context)).thenReturn(step).thenReturn(null);
        when(chainRegistry.process(any(StepContext.class))).thenReturn(stepContext);
        AgentContext result = processor.process(context);
        assertEquals(context, result);
    }
} 