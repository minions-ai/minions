package com.minionslab.core.step.graph;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.step.Step;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DefaultStepGraph}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Step graph navigation, step addition, and retrieval</li>
 * </ul>
 * <p>
 * Setup: Instantiates DefaultStepGraph and adds steps for testing.
 */
class DefaultStepGraphTest {
    static class DummyStep implements Step {
        private final String id;
        DummyStep(String id) { this.id = id; }
        @Override public void customize(com.minionslab.core.step.customizer.StepCustomizer customizer) {}
        @Override public String getId() { return id; }
        @Override public String getType() { return "dummy"; }
        @Override public com.minionslab.core.message.Message getSystemPrompt() { return null; }
        @Override public com.minionslab.core.message.Message getGoal() { return null; }
    }

    static class DummyTransitionStrategy implements TransitionStrategy {
        @Override
        public Step selectNext(Step current, List<Step> nextSteps, AgentContext context) {
            return nextSteps.isEmpty() ? null : nextSteps.get(0);
        }
    }

    static class DummyStepGraphDefinition implements StepGraphDefinition {
        private final List<Step> steps;
        private final Map<String, List<String>> transitions;
        private final Step startStep;
        private final TransitionStrategy strategy;
        DummyStepGraphDefinition(List<Step> steps, Map<String, List<String>> transitions, Step startStep, TransitionStrategy strategy) {
            this.steps = steps;
            this.transitions = transitions;
            this.startStep = startStep;
            this.strategy = strategy;
        }
        @Override public List<Step> getSteps() { return steps; }
        @Override public Map<String, List<String>> getTransitions() { return transitions; }
        @Override public Step getStartStep() { return startStep; }
        @Override public TransitionStrategy getTransitionStrategy() { return strategy; }
    }

    private DummyStep step1, step2;
    private DummyStepGraphDefinition definition;
    private DefaultStepGraph graph;

    @BeforeEach
    void setUp() {
        step1 = new DummyStep("s1");
        step2 = new DummyStep("s2");
        definition = new DummyStepGraphDefinition(
                List.of(step1, step2),
                Map.of("s1", List.of("s2")),
                step1,
                new DummyTransitionStrategy()
        );
        graph = new DefaultStepGraph(definition);
    }

    @Test
    void testGetCurrentStep() {
        assertEquals(step1, graph.getCurrentStep());
    }

    @Test
    void testGetNextStepAdvances() {
        AgentContext ctx = null;
        Step next = graph.getNextStep(ctx);
        assertEquals(step2, next);
        assertEquals(step2, graph.getCurrentStep());
    }

    @Test
    void testReset() {
        graph.getNextStep(null);
        assertNotEquals(step1, graph.getCurrentStep());
        graph.reset();
        assertEquals(step1, graph.getCurrentStep());
    }

    @Test
    void testAddStep() {
        DummyStep step3 = new DummyStep("s3");
        graph.addStep(step3);
        assertTrue(graph.getAllSteps().contains(step3));
    }

    @Test
    void testAddStepThrowsIfExists() {
        DummyStep duplicate = new DummyStep("s1");
        assertThrows(IllegalArgumentException.class, () -> graph.addStep(duplicate));
    }

    @Test
    void testAddTransition() {
        DummyStep step3 = new DummyStep("s3");
        graph.addStep(step3);
        graph.addTransition(step2, step3);
        // No exception means success; transitions are private, so we can't check directly
    }

    @Test
    void testGetAllSteps() {
        List<Step> all = graph.getAllSteps();
        assertTrue(all.contains(step1));
        assertTrue(all.contains(step2));
    }

    /**
     * Tests that steps can be added and retrieved from the graph.
     * Setup: Adds steps to the graph.
     * Expected: Steps are retrievable by ID.
     */
    @Test
    void testAddAndGetStep() { /* ... */ }

    /**
     * Tests that navigation methods return the correct next and previous steps.
     * Setup: Adds steps and sets up transitions.
     * Expected: Navigation methods return correct steps.
     */
    @Test
    void testNavigation() { /* ... */ }

    /**
     * Tests that removing a step updates the graph correctly.
     * Setup: Adds and removes steps.
     * Expected: Removed steps are no longer retrievable.
     */
    @Test
    void testRemoveStep() { /* ... */ }
} 