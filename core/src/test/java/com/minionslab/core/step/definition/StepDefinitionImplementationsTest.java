package com.minionslab.core.step.definition;

import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.step.impl.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StepDefinitionImplementationsTest {
    @Test
    void testModelCallStepDefinition() {
        ModelCallStepDefinition def = new ModelCallStepDefinition();
        def.setPromptTemplate("template");
        def.setGoal(DefaultMessage.builder().content("goal").role(MessageRole.GOAL).build());
        ModelCallStep step = def.buildStep();
        assertNotNull(step);
        assertTrue(step instanceof ModelCallStep);
    }

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

    @Test
    void testSummarizeStepDefinition() {
        SummarizeStepDefinition def = new SummarizeStepDefinition();
        def.setSourceStepMessages(List.of(DefaultMessage.builder().content("msg").role(MessageRole.USER).build()));
        def.setSummaryTemplate("summary");
        SummarizeStep step = def.buildStep();
        assertNotNull(step);
        assertTrue(step instanceof SummarizeStep);
    }

    @Test
    void testPlannerStepDefinition() {
        PlannerStepDefinition def = new PlannerStepDefinition();
        def.setConstraints("none");
        def.setPlannerName("simple");
        PlannerStep step = def.buildStep();
        assertNotNull(step);
        assertTrue(step instanceof PlannerStep);
    }

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

    @Test
    void testSetEntityStepDefinition() {
        SetEntityStepDefinition def = new SetEntityStepDefinition();
        def.setEntity("Patient");
        def.setKeyValueMap(Map.of("name", "John"));
        SetEntityStep step = def.buildStep();
        assertNotNull(step);
        assertTrue(step instanceof SetEntityStep);
    }

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