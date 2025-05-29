package com.minionslab.core.step;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.agent.AgentRecipe;
import com.minionslab.core.step.graph.StepGraph;
import com.minionslab.core.step.graph.StepGraphCompletionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StepManagerTest {
    @Mock(strictness = Mock.Strictness.LENIENT)
    private StepGraph stepGraph;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private StepGraphCompletionStrategy completionStrategy;
    private StepManager stepManager;
    @Mock
    private Step step;
    @Mock
    private AgentContext agentContext;
    @Mock
    private AgentRecipe recipe;
    
    @BeforeEach
    void setUp() {

        when(stepGraph.getCurrentStep()).thenReturn(step);
        when(completionStrategy.isComplete(any(), any(), any())).thenReturn(false);
        when(recipe.getStepGraph()).thenReturn(stepGraph);
        when(recipe.getCompletionStrategy()).thenReturn(completionStrategy);

        stepManager = new StepManager(recipe);
    }

    @Test
    void testGetCurrentStep() {
        assertEquals(step, stepManager.getCurrentStep());
        verify(stepGraph,times(1)).getCurrentStep();
    }

    @Test
    void testIsWorkflowCompleteFalse() {
        when(completionStrategy.isComplete(any(), any(), any())).thenReturn(false);
        assertFalse(stepManager.isWorkflowComplete());
    }

    @Test
    void testIsWorkflowCompleteTrue() {
        when(completionStrategy.isComplete(any(), any(), any())).thenReturn(true);
        // Force workflowComplete to false so it checks again
        StepManager manager = new StepManager(recipe);
        assertTrue(manager.isWorkflowComplete());
    }

    @Test
    void testAdvanceToNextStep() {
        stepManager.advanceToNextStep(agentContext);
        verify(stepGraph).advanceToNextStep(agentContext);
    }

    @Test
    void testSetWorkflowComplete() {
        stepManager.setWorkflowComplete();
        verify(stepGraph).complete();
        assertTrue(stepManager.isWorkflowComplete());
    }
} 