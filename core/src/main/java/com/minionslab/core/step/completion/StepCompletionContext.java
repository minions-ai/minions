package com.minionslab.core.step.completion;

import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.chain.ProcessResult;
import com.minionslab.core.step.Step;
import com.minionslab.core.step.StepContext;
import com.minionslab.core.step.graph.StepGraph;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class StepCompletionContext<T extends ProcessResult> implements ProcessContext<T> {
    public boolean plannerOverridden;
    private Step currectStep;
    private StepGraph stepGraph;
    private List<T> results = new ArrayList<>();
    private StepContext stepContext;
    private int maxModelCallLimit;
    private boolean isComplete;
    private String modelSignaledCompletionOutput;
    private String checkerToolOutput;
    private long timeout;
    private boolean externalAbortSignalReceived;
    private int maxRetryCount;
    private boolean guardrailTriggered;
    private boolean memoryUpdateFailed;
    private Throwable error;
    private boolean goalAchieved;
    
    
    @Override
    public void addResult(T result) {
    
    }
    

}
