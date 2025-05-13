package com.minionslab.core.step;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;

class StepManagerTest {
    
    private Step step1;
    private Step step2;
    private Step step3;
    private Step step4;
    private List<Step> steps;
    private Map<String, List<String>> stepGraphMap;
    private DefaultStepGraph stepGraph;
    private StepManager stepManager;
    
    
    @BeforeEach
    void setUp() {
        MessageBundle bundle1 = new MessageBundle();
        bundle1.addMessage(DefaultMessage.builder()
                .role(MessageRole.SYSTEM)
                .scope(MessageScope.STEP)
                .content("system 1")
                .build());
        MessageBundle bundle2 = new MessageBundle();
        bundle2.addMessage(DefaultMessage.builder()
                .role(MessageRole.SYSTEM)
                .scope(MessageScope.STEP)
                .content("system 2")
                .build());
        MessageBundle bundle3 = new MessageBundle();
        bundle3.addMessage(DefaultMessage.builder()
                .role(MessageRole.SYSTEM)
                .scope(MessageScope.STEP)
                .content("system 3")
                .build());
        MessageBundle bundle4 = new MessageBundle();
        bundle4.addMessage(DefaultMessage.builder()
                .role(MessageRole.SYSTEM)
                .scope(MessageScope.STEP)
                .content("system 4")
                .build());
        step1 = new DefaultStep("s1", bundle1, Set.of());
        step2 = new DefaultStep("s2", bundle2, Set.of());
        step3 = new DefaultStep("s3", bundle3, Set.of());
        step4 = new DefaultStep("s4", bundle4, Set.of());
        steps = List.of(step1, step2, step3);
        stepGraphMap = new HashMap<>();
        stepGraphMap.put("s1", List.of("s2", "s3"));
        stepGraphMap.put("s2", List.of("s4"));
        stepGraphMap.put("s3", List.of());
        stepGraph = new DefaultStepGraph(steps, stepGraphMap, new NextStepDecisionChain());
        stepManager = new StepManager(stepGraph);
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
    void testSetCurrentStepByIdInvalid() {
        stepManager.setCurrentStep("invalid");
        assertNull(stepManager.getCurrentStep());
        assertTrue(stepManager.isWorkflowComplete());
    }
    
    @Test
    void testSetWorkflowComplete() {
        stepManager.setWorkflowComplete();
        assertNull(stepManager.getCurrentStep());
        assertTrue(stepManager.isWorkflowComplete());
    }
    
    @Test
    void testReset() {
        stepManager.setCurrentStep(step3);
        stepManager.setWorkflowComplete();
        stepManager = new StepManager(stepGraph);
        assertEquals(step1, stepManager.getCurrentStep());
        assertFalse(stepManager.isWorkflowComplete());
    }
}