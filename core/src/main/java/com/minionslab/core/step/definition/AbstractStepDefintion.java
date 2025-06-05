package com.minionslab.core.step.definition;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.minionslab.core.message.Message;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.impl.AbstractStep;
import lombok.Data;
import lombok.experimental.Accessors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Accessors
public abstract class AbstractStepDefintion<T extends Step> implements StepDefinition<T> {
    
    private String id;
    private Message goal;
    private String type;
    
    // Getters and setters
    
    public void configureStep(AbstractStep step) {
        step.setId(id);
        step.setGoal(goal);

    }
    
    

}

