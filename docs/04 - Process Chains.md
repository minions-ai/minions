# Process Chains

Minions uses the **Chain of Responsibility** pattern to handle agent behaviors in a modular way. This pattern allows for flexible composition of behaviors and easy extension of functionality.

## Chains are used in:

- **Step Completion Evaluation**
- **Memory Management**
  - Memory Summarization
  - Memory Query Execution
  - Memory Persistence
- **Tool Selection**
- **Custom Output Evaluation**

Each chain is made of `Processor<T extends ProcessContext>` implementations that can be prioritized and conditionally applied.

## Memory Processors

### Memory Summarizer Processor

```java
@Component
public class MemorySummarizerProcessor implements Processor<AgentContext> {
    private final ModelCallService modelCallService;
    
    @Override
    public AgentContext process(AgentContext input) {
        // Query memory based on context
        // Generate summaries using model calls
        // Store summaries in appropriate subsystems
        return input;
    }
}
```

### Memory Query Processors

```java
@Component
public class StepVectorQueryStrategy implements MemoryQueryStrategy {
    @Override
    public MemoryQuery getMemoryQuery(ProcessContext context) {
        // Build and return memory query
    }
    
    @Override
    public List<MemorySubsystem> getSupportedSubsystem() {
        return List.of(MemorySubsystem.VECTOR);
    }
}
```

## Defining a Processor

```java
@Component
public class MaxModelCallCountProcessor
        extends AbstractProcessor<StepGraphCompletionContext<ProcessResult<StepCompletionOutcome>>, StepCompletionOutcome> {

    @Override
    public StepCompletionOutcome doProcess(StepGraphCompletionContext input) {
        // Custom logic
    }
}
```

## Processor Configuration

Processors can be configured through:

1. **Spring Configuration**
```java
@Configuration
public class ProcessorConfig {
    @Bean
    public Processor<AgentContext> memorySummarizerProcessor(ModelCallService modelCallService) {
        return new MemorySummarizerProcessor(modelCallService);
    }
}
```

2. **Customizers**
```java
@Component
public class MemoryProcessorCustomizer implements ProcessorCustomizer<MemorySummarizerProcessor> {
    @Override
    public void customize(MemorySummarizerProcessor processor) {
        // Custom configuration
    }
}
```

## Testing Processors

```java
@ExtendWith(MockitoExtension.class)
class MemorySummarizerProcessorTest {
    @Mock
    private AgentContext context;
    @Mock
    private ModelCallService modelCallService;
    
    @Test
    void testProcess() {
        MemorySummarizerProcessor processor = new MemorySummarizerProcessor(modelCallService);
        AgentContext result = processor.process(context);
        // Verify behavior
    }
}
```

Processors can expose **Customizers** to inject logic conditionally during Spring bootstrapping.