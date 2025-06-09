package com.minionslab.core.step.definition;

import com.minionslab.core.common.message.MessageRole;
import com.minionslab.core.common.message.SimpleMessage;
import com.minionslab.core.step.impl.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for StepDefinition implementations.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Each StepDefinition implementation builds the correct Step type</li>
 * </ul>
 * <p>
 * Setup: Instantiates each StepDefinition implementation and calls buildStep().
 */
class StepDefinitionImplementationsTest {
    /**
     * Tests that ModelCallStepDefinition builds a ModelCallStep.
     * Setup: Sets prompt template and goal.
     * Expected: buildStep returns a ModelCallStep instance.
     */
    @Test
    void testModelCallStepDefinition() {
        ModelCallStepDefinition def = new ModelCallStepDefinition();
        def.setPromptTemplate("template");
        def.setGoal(SimpleMessage.builder().content("goal").role(MessageRole.GOAL).build());
        ModelCallStep step = def.buildStep();
        assertNotNull(step);
        assertTrue(step instanceof ModelCallStep);
    }

    /**
     * Tests that AskUserStepDefinition builds an AskUserStep.
     * Setup: Sets question, inputType, and optional.
     * Expected: buildStep returns an AskUserStep instance.
     */
    @Test
    void testAskUserStepDefinition() {
        AskUserStepDefinition def = new AskUserStepDefinition();
        def.setQuestion("What is your name?");
        def.setInputType("text");
        def.setOptional(false);
        AskUserStep step = def.buildStep();
        assertNotNull(step);
        assertTrue(step instanceof AskUserStep);
    }

    /**
     * Tests that SummarizeStepDefinition builds a SummarizeStep.
     * Setup: Sets sourceStepMessages and summaryTemplate.
     * Expected: buildStep returns a SummarizeStep instance.
     */
    @Test
    void testSummarizeStepDefinition() {
        SummarizeStepDefinition def = new SummarizeStepDefinition();
        def.setSourceStepMessages(List.of(SimpleMessage.builder().content("msg").role(MessageRole.USER).build()));
        def.setSummaryTemplate("summary");
        SummarizeStep step = def.buildStep();
        assertNotNull(step);
        assertTrue(step instanceof SummarizeStep);
    }

    /**
     * Tests that PlannerStepDefinition builds a PlannerStep.
     * Setup: Sets constraints and plannerName.
     * Expected: buildStep returns a PlannerStep instance.
     */
    @Test
    void testPlannerStepDefinition() {
        PlannerStepDefinition def = new PlannerStepDefinition();
        def.setConstraints("none");
        def.setPlannerName("simple");
        PlannerStep step = def.buildStep();
        assertNotNull(step);
        assertTrue(step instanceof PlannerStep);
    }

    /**
     * Tests that EvaluateStepDefinition builds an EvaluateStep.
     * Setup: Sets criteria, targetStepId, and promptTemplate.
     * Expected: buildStep returns an EvaluateStep instance.
     */
    @Test
    void testEvaluateStepDefinition() {
        EvaluateStepDefinition def = new EvaluateStepDefinition();
        def.setCriteria("criteria");
        def.setTargetStepId("step1");
        def.setPromptTemplate("prompt");
        EvaluateStep step = def.buildStep();
        assertNotNull(step);
        assertTrue(step instanceof EvaluateStep);
    }

    /**
     * Tests that SetEntityStepDefinition builds a SetEntityStep.
     * Setup: Sets entity and keyValueMap.
     * Expected: buildStep returns a SetEntityStep instance.
     */
    @Test
    void testSetEntityStepDefinition() {
        SetEntityStepDefinition def = new SetEntityStepDefinition();
        def.setEntity("Patient");
        def.setKeyValueMap(Map.of("name", "John"));
        SetEntityStep step = def.buildStep();
        assertNotNull(step);
        assertTrue(step instanceof SetEntityStep);
    }

    /**
     * Tests that BranchStepDefinition builds a BranchStep.
     * Setup: Sets conditionExpr, thenSteps, and elseSteps.
     * Expected: buildStep returns a BranchStep instance.
     */
    @Test
    void testBranchStepDefinition() {
        BranchStepDefinition def = new BranchStepDefinition();
        def.setConditionExpr("x > 0");
        def.setThenSteps(List.of());
        def.setElseSteps(List.of());
        BranchStep step = def.buildStep();
        assertNotNull(step);
        assertTrue(step instanceof BranchStep);
    }
} 