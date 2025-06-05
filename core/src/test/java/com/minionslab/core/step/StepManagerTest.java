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

/**
 * Unit tests for {@link StepManager}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Get current step</li>
 *   <li>Check workflow completion (true/false)</li>
 *   <li>Advance to next step</li>
 *   <li>Set workflow complete</li>
 * </ul>
 * <p>
 * Setup: Mocks StepGraph, StepGraphCompletionStrategy, AgentContext, AgentRecipe, and Step. Initializes StepManager with a mocked recipe.
 */
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
    
    /**
     * Sets up the test environment before each test.
     * Mocks dependencies and initializes StepManager.
     * Expected: StepManager is ready for use in each test.
     */
    @BeforeEach
    void setUp() {

        when(stepGraph.getCurrentStep()).thenReturn(step);
        when(completionStrategy.isComplete(any(), any(), any())).thenReturn(false);
        when(recipe.getStepGraph()).thenReturn(stepGraph);
        when(recipe.getCompletionStrategy()).thenReturn(completionStrategy);

        stepManager = new StepManager(recipe);
    }

    /**
     * Tests that getCurrentStep returns the current step from StepGraph.
     * Setup: StepGraph returns a mocked step.
     * Expected: StepManager returns the same step.
     */
    @Test
    void testGetCurrentStep() {
        assertEquals(step, stepManager.getCurrentStep());
        verify(stepGraph,times(1)).getCurrentStep();
    }

    /**
     * Tests that isWorkflowComplete returns false when completion strategy is false.
     * Setup: Completion strategy returns false.
     * Expected: isWorkflowComplete returns false.
     */
    @Test
    void testIsWorkflowCompleteFalse() {
        when(completionStrategy.isComplete(any(), any(), any())).thenReturn(false);
        assertFalse(stepManager.isWorkflowComplete());
    }

    /**
     * Tests that isWorkflowComplete returns true when completion strategy is true.
     * Setup: Completion strategy returns true.
     * Expected: isWorkflowComplete returns true.
     */
    @Test
    void testIsWorkflowCompleteTrue() {
        when(completionStrategy.isComplete(any(), any(), any())).thenReturn(true);
        // Force workflowComplete to false so it checks again
        StepManager manager = new StepManager(recipe);
        assertTrue(manager.isWorkflowComplete());
    }

    /**
     * Tests that advanceToNextStep calls StepGraph.advanceToNextStep.
     * Setup: StepManager and AgentContext are initialized.
     * Expected: StepGraph.advanceToNextStep is called.
     */
    @Test
    void testAdvanceToNextStep() {
        stepManager.advanceToNextStep(agentContext);
        verify(stepGraph).advanceToNextStep(agentContext);
    }

    /**
     * Tests that setWorkflowComplete calls StepGraph.complete and sets workflowComplete.
     * Setup: StepManager is initialized.
     * Expected: StepGraph.complete is called and workflowComplete is true.
     */
    @Test
    void testSetWorkflowComplete() {
        stepManager.setWorkflowComplete();
        verify(stepGraph).complete();
        assertTrue(stepManager.isWorkflowComplete());
    }
} 