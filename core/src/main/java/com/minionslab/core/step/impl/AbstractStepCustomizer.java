package com.minionslab.core.step.impl;

import com.minionslab.core.message.Message;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.customizer.StepCustomizer;

public class AbstractStepCustomizer implements StepCustomizer {
    private final String id;
    private final Message goal;
    private final String promptTemplate;
    private final Message systemPrompt;

    public AbstractStepCustomizer(String id, Message goal, String promptTemplate, Message systemPrompt) {
        this.id = id;
        this.goal = goal;
        this.promptTemplate = promptTemplate;
        this.systemPrompt = systemPrompt;
    }

    @Override
    public void customize(Step step) {
        if (step instanceof AbstractStep abstractStep) {
            if (id != null) abstractStep.setId(id);
            if (goal != null) abstractStep.setGoal(goal);
            if (promptTemplate != null) abstractStep.setPromptTemplate(promptTemplate);
            if (systemPrompt != null) abstractStep.setSystemPrompt(systemPrompt);
        }
    }
} 