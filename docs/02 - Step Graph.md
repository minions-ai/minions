# Step Graph

A **Step Graph** defines the structured flow of logic an agent follows to reach its goal. Unlike ad hoc prompt chaining, Minions formalizes execution paths using a graph of steps connected by transitions.

## Components

- **Step**: A unit of logic or decision. A step can:
  - Invoke a model
  - Execute a tool call
  - Query or update memory
  - Make a transition decision
- **StepGraphDefinition**: Declarative definition of steps and their transitions
- **TransitionStrategy**: Defines how the agent determines the next step
  - Sequential transitions
  - Conditional transitions
  - Dynamic transitions based on memory state
- **Start Step / Terminal Step**: Every graph has an entry point and can define one or more terminal states

## Memory-Aware Steps

Steps can interact with memory subsystems:

```java
@Component
public class MemoryAwareStep implements Step {
    private final MemoryManager memoryManager;
    
    @Override
    public StepResult execute(StepContext context) {
        // Query memory
        List<Message> memory = memoryManager.query(context);
        
        // Store results
        memoryManager.store(MemorySubsystem.EPISODIC, result);
        
        return StepResult.builder()
            .outcome(StepCompletionOutcome.COMPLETE)
            .build();
    }
}
```

## Step Execution Flow

1. Agent starts at the defined start step
2. Executes step logic:
   - Prompts the model
   - Triggers a tool
   - Queries memory subsystems
   - Processes memory results
3. Determines the next step using a transition strategy
4. Repeats until a terminal step is reached or a completion condition is satisfied

## Memory Integration

Steps can integrate with memory in several ways:

1. **Memory Query**
```java
MemoryQuery query = MemoryQuery.builder()
    .expression(Expr.eq("conversationId", context.getConversationId()))
    .limit(10)
    .build();
List<Message> results = memoryManager.query(query);
```

2. **Memory Storage**
```java
memoryManager.store(MemorySubsystem.EPISODIC, message);
memoryManager.store(MemorySubsystem.VECTOR, message);
```

3. **Memory Summarization**
```java
@Autowired
private MemorySummarizerProcessor summarizer;

public StepResult execute(StepContext context) {
    summarizer.process(context);
    // Continue with step logic
}
```

## Customizing Step Graphs

Minions supports a Definition/Customizer pattern:

- **Definition** defines the logic and structure
- **Customizers** allow injection of additional behavior:
  - Memory query strategies
  - Memory summarization rules
  - Persistence configurations
  - Transitions based on memory state

This allows default agent flows to be extended in context-aware ways—e.g., adding memory-aware retry steps or custom memory processing logic.