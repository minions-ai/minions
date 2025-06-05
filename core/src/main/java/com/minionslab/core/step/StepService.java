package com.minionslab.core.step;

import java.util.List;

public interface StepService {
    Step getCurrentStep(StepContext context);
    Step getNextStep(StepContext context);
    void advanceToNextStep(StepContext context);
    void resetSteps(StepContext context);

    StepContext executeStep(StepContext context);

    StepStatus getStepStatus(StepContext context);
    List<Step> getAllSteps(StepContext context);

    boolean isWorkflowComplete(StepContext context);
    void setWorkflowComplete(StepContext context);
}
