package com.minionslab.mcp.step;

import com.minionslab.mcp.agent.AgentRecipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StepManagerTest {
    
    private Step step1;
    private Step step2;
    private Step step3;
    private Step step4;
    private List<Step> steps;
    private Map<String, List<String>> stepGraph;
    private StepManager stepManager;

    
    @BeforeEach
    void setUp() {
        step1 = new DefaultStep("s1", "Step 1", Set.of(), "prompt1");
        step2 = new DefaultStep("s2", "Step 2", Set.of(), "prompt2");
        step3 = new DefaultStep("s3", "Step 3", Set.of(), "prompt3");
        step4 = new DefaultStep("s4", "Step 4", Set.of(), "prompt4");
        steps = List.of(step1, step2, step3);
        stepGraph = new HashMap<>();
        stepGraph.put("s1", List.of("s2", "s3"));
        stepGraph.put("s2", List.of("s4"));
        stepGraph.put("s3", List.of());
        stepManager = new StepManager(steps, stepGraph);
    }
    
    @Test
    void testInitializationSetsFirstStep() {
        assertEquals(step1, stepManager.getCurrentStep());
        assertFalse(stepManager.isWorkflowComplete());
    }
    
    @Test
    void testSetCurrentStepByObject() {
        stepManager.setCurrentStep(step2);
        assertEquals(step2, stepManager.getCurrentStep());
        assertFalse(stepManager.isWorkflowComplete());
    }
    
    @Test
    void testSetCurrentStepById() {
        stepManager.setCurrentStep("s2");
        assertEquals(step2, stepManager.getCurrentStep());
        assertFalse(stepManager.isWorkflowComplete());
    }
    
    @Test
    void testSetCurrentStepToNullMarksComplete() {
        stepManager.setCurrentStep((Step) null);
        assertNull(stepManager.getCurrentStep());
        assertTrue(stepManager.isWorkflowComplete());
    }
    
    @Test
    void testAdvanceToNextStepSingleBranch() {
        // s1 -> s2
        stepManager.setCurrentStep(step1);
        stepManager.getInstructionsToExecute().add(new Step.StepInstruction("s1", "result", Step.StepOutcome.COMPLETED, null, null, null, 1.0, "s2"));
        stepManager.advanceToNextStep();
        assertEquals(step2, stepManager.getCurrentStep());
    }
    
    @Test
    void testAdvanceToNextStepMultipleBranches() {
        // s1 -> s2 or s3, instruction suggests s3
        stepManager.setCurrentStep(step1);
        stepManager.getInstructionsToExecute().add(new Step.StepInstruction("s1", "result", Step.StepOutcome.COMPLETED, null, null, null, 1.0, "s3"));
        stepManager.advanceToNextStep();
        assertEquals(step3, stepManager.getCurrentStep());
    }
    
    @Test
    void testAdvanceToNextStepNoNextSteps() {
        // s3 has no next steps
        stepManager.setCurrentStep(step3);
        stepManager.advanceToNextStep();
        assertNull(stepManager.getCurrentStep());
        assertTrue(stepManager.isWorkflowComplete());
    }
    
    @Test
    void testGetPossibleNextSteps() {
        stepManager.setCurrentStep(step1);
        List<Step> possible = stepManager.getPossibleNextSteps();
        assertTrue(possible.contains(step2));
        assertTrue(possible.contains(step3));
    }
    
    @Test
    void testReset() {
        stepManager.setCurrentStep(step3);
        stepManager.reset();
        assertEquals(step1, stepManager.getCurrentStep());
        assertFalse(stepManager.isWorkflowComplete());
        assertTrue(stepManager.getInstructions().isEmpty());
        assertTrue(stepManager.getInstructionsToExecute().isEmpty());
    }
    
    @Test
    void testAddAndRemoveInstruction() {
        Step.StepInstruction instruction = new Step.StepInstruction("s1", "result", Step.StepOutcome.COMPLETED, null, null, null, 1.0, "s2");
        stepManager.addInstruction(instruction);
        assertTrue(stepManager.getInstructions().contains(instruction));
        assertTrue(stepManager.getInstructionsToExecute().contains(instruction));
        stepManager.removeInstruction(instruction);
        assertFalse(stepManager.getInstructions().contains(instruction));
        stepManager.removeInstructionToExecute(instruction);
        assertFalse(stepManager.getInstructionsToExecute().contains(instruction));
    }
    
    @Test
    void testSetCurrentStepByIdInvalid() {
        stepManager.setCurrentStep("invalid");
        assertNull(stepManager.getCurrentStep());
        assertTrue(stepManager.isWorkflowComplete());
    }
    
    @Test
    void testGetNextStepSuggestion() {
        List<Step> possibleNext = List.of(step2, step3);
        List<Step.StepInstruction> instructions = List.of(
                new Step.StepInstruction("s1", "result", Step.StepOutcome.COMPLETED, null, null, null, 1.0, "s3")
                                                         );
        String suggestion = stepManager.getNextStepSuggestion(possibleNext, instructions);
        assertEquals("s3", suggestion);
    }
    
    @Test
    void testEmptySteps() {
        StepManager emptyManager = new StepManager(Collections.emptyList(), Collections.emptyMap());
        emptyManager.setWorkflowComplete();
        assertNull(emptyManager.getCurrentStep());
        assertTrue(emptyManager.isWorkflowComplete());
        assertTrue(emptyManager.getSteps().isEmpty());
    }
    
    @Test
    void testAddStep() {
        StepManager manager = new StepManager(new ArrayList<>(), new HashMap<>());
        Step newStep = new DefaultStep("s4", "desc4", Set.of(), "prompt4");
        manager.addStep(newStep);
        assertTrue(manager.getSteps().contains(newStep));
    }
}