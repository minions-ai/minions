# Tool Integration

Agents often rely on tools to gather data, make external API calls, or trigger workflows. Tools can also interact with memory subsystems to store and retrieve information.

## Defining a Tool

A tool is a Spring Bean implementing `Tool<TInput, TOutput>`. It must define:

- The **name** and **input schema**
- Execution logic (usually async)
- Optionally: tool metadata and dynamic arguments
- Memory integration points

## Memory-Aware Tools

Tools can interact with memory subsystems:

```java
@Component
public class MemoryAwareTool implements Tool<ToolInput, ToolOutput> {
    private final MemoryManager memoryManager;
    
    @Override
    public ToolOutput execute(ToolInput input) {
        // Query memory for context
        List<Message> context = memoryManager.query(input.getContext());
        
        // Execute tool logic
        ToolOutput output = process(input, context);
        
        // Store results in memory
        memoryManager.store(MemorySubsystem.EPISODIC, output.toMessage());
        
        return output;
    }
}
```

## Tool Usage Flow

1. Step issues a tool call request
2. Tool resolves parameters and executes logic:
   - Queries memory for context
   - Performs tool-specific operations
   - Stores results in memory
3. Output is passed to the agent and can be:
   - Stored in memory subsystems
   - Used for reasoning
   - Summarized for long-term storage

## Memory Integration Points

Tools can integrate with memory at several points:

1. **Pre-execution**
```java
// Query memory for context
MemoryQuery query = MemoryQuery.builder()
    .expression(Expr.eq("toolId", toolId))
    .limit(5)
    .build();
List<Message> context = memoryManager.query(query);
```

2. **Post-execution**
```java
// Store tool results
Message result = Message.builder()
    .content(output.toString())
    .metadata("toolId", toolId)
    .build();
memoryManager.store(MemorySubsystem.EPISODIC, result);
```

3. **Error Handling**
```java
try {
    return execute(input);
} catch (Exception e) {
    // Store error in memory
    Message error = Message.builder()
        .content(e.getMessage())
        .metadata("error", true)
        .build();
    memoryManager.store(MemorySubsystem.EPISODIC, error);
    throw e;
}
```

## Testing Tools

```java
@ExtendWith(MockitoExtension.class)
class MemoryAwareToolTest {
    @Mock
    private MemoryManager memoryManager;
    
    @Test
    void testExecute() {
        MemoryAwareTool tool = new MemoryAwareTool(memoryManager);
        ToolInput input = createTestInput();
        
        when(memoryManager.query(any())).thenReturn(List.of());
        ToolOutput output = tool.execute(input);
        
        verify(memoryManager).query(any());
        verify(memoryManager).store(eq(MemorySubsystem.EPISODIC), any());
        assertNotNull(output);
    }
}
```