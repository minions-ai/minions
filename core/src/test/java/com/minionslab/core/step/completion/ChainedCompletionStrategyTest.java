package com.minionslab.core.step.completion;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.graph.StepGraph;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChainedCompletionStrategyTest {
    @Test
    void testIsCompleteReturnsFalseAndProcessesChain() {
        ChainedCompletionStrategy strategy = new ChainedCompletionStrategy();
        StepGraph graph = mock(StepGraph.class);
        Step step = mock(Step.class);
        AgentContext context = mock(AgentContext.class);
        // Should not throw and should always return false
        assertFalse(strategy.isComplete(graph, step, context));
    }
} 