package com.minionslab.core.step.definition;

import com.minionslab.core.common.message.Message;

class DummyStepDefinition implements StepDefinition<DummyStepDefinition.DummyStep> {
    @Override
    public DummyStep buildStep() {
        return new DummyStep();
    }
    
    @Override
    public String getType() {
        return "dummy";
    }
    
    @Override
    public String getDescription() {
        return "Dummy step definition for testing.";
    }
    
    static class DummyStep implements com.minionslab.core.step.Step {
        @Override
        public void customize(com.minionslab.core.step.customizer.StepCustomizer customizer) {
        }
        
        @Override
        public String getId() {
            return "dummy-id";
        }
        
        @Override
        public String getType() {
            return "dummy";
        }
        
        @Override
        public Message getSystemPrompt() {
            return null;
        }
        
        @Override
        public Message getGoal() {
            return null;
        }
    }
}
