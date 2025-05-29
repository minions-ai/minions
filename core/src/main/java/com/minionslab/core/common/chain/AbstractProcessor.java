package com.minionslab.core.common.chain;

import com.minionslab.core.step.completion.StepCompletionContext;

import java.time.Instant;
import java.util.List;

public abstract class AbstractProcessor<T extends ProcessContext, C > implements Processor<T> {
    protected String id;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public final T process(T input) {
        Instant start = Instant.now();
        
        ProcessResult<C> result = null;
        C c = null;
        try {
            c = doProcess(input);
            if (c == null) {
                result = ProcessResult.skipped(this.id);
            } else {
                result = ProcessResult.success(this.id, List.of(c), start);
            }
        } catch (Exception e) {
            result = ProcessResult.failure(this.id, e, start);
        }
        
        input.addResult(result);
        return input;
    }
    
    protected abstract C doProcess(T input) throws Exception;
    
    @Override
    public boolean accepts(T input) {
        return input != null;
    }
}
