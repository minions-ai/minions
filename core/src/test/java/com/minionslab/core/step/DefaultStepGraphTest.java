package com.minionslab.core.step;

import com.minionslab.core.context.AgentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultStepGraphTest {
    private Step stepA;
    private Step stepB;
    private Step stepC;
    private List<Step> steps;
    private Map<String, List<String>> adjacency;
    private NextStepDecisionChain decisionChain;
    private DefaultStepGraph graph;

    @BeforeEach
    void setUp() {
        stepA = mock(Step.class);
        when(stepA.getId()).thenReturn("A");
        stepB = mock(Step.class);
        when(stepB.getId()).thenReturn("B");
        stepC = mock(Step.class);
        when(stepC.getId()).thenReturn("C");
        steps = List.of(stepA, stepB, stepC);
        adjacency = new HashMap<>();
        adjacency.put("A", List.of("B", "C"));
        adjacency.put("B", List.of("C"));
        adjacency.put("C", List.of());
        decisionChain = mock(NextStepDecisionChain.class);
        graph = new DefaultStepGraph(steps, adjacency, decisionChain);
    }

    @Test
    void testGetPossibleNextSteps() {
        List<Step> nextFromA = graph.getPossibleNextSteps(stepA);
        assertEquals(2, nextFromA.size());
        assertTrue(nextFromA.contains(stepB));
        assertTrue(nextFromA.contains(stepC));

        List<Step> nextFromB = graph.getPossibleNextSteps(stepB);
        assertEquals(1, nextFromB.size());
        assertTrue(nextFromB.contains(stepC));

        List<Step> nextFromC = graph.getPossibleNextSteps(stepC);
        assertTrue(nextFromC.isEmpty());
    }

    @Test
    void testSelectNextStepDelegatesToDecisionChain() {
        List<Step> possibleNext = graph.getPossibleNextSteps(stepA);
        AgentContext context = mock(AgentContext.class);
        Step expected = stepB;
        when(decisionChain.decide(eq(stepA), eq(possibleNext), eq(context), any())).thenReturn(expected);
        Step result = graph.selectNextStep(stepA, possibleNext, context, null);
        assertEquals(expected, result);
        verify(decisionChain, times(1)).decide(eq(stepA), eq(possibleNext), eq(context), any());
    }
} 