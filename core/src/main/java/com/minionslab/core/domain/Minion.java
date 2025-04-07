package com.minionslab.core.domain;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

/**
 * Concrete implementation of AbstractMinion that provides default behavior.
 */
@Document("minion")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Minion extends AbstractMinion {
    
    @DocumentReference
    private MinionPrompt prompt;
    
    private String tenantId;

    @Override
    protected FunctionCallback[] getAvailableTools() {
        return new FunctionCallback[0];
    }
}