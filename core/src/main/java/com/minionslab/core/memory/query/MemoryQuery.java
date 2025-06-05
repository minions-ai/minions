package com.minionslab.core.memory.query;

import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.query.expression.MemoryQueryExpression;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Query object for retrieving messages from memory, supporting various filters and options.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode
@AllArgsConstructor
@Builder
@ToString
public class MemoryQuery {
    
    // Optional: execution parameters
    private MemorySubsystem subsystems;
    private int limit;
    private MemoryQueryExpression expression;
    
    // --- Constructors and Getters ---

    
    public MemoryQuery(@NotBlank MemorySubsystem subsystem, int limit) {
        this.subsystems = subsystem;
        this.limit = limit;
    }
    

    
}
