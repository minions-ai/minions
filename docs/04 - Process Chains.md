Minions uses the **Chain of Responsibility** pattern to handle agent behaviors in a modular way.

## Chains are used in:

- **Step Completion Evaluation**
- **Memory Summarization**
- **Tool Selection**
- **Custom Output Evaluation**

Each chain is made of `Processor<T extends ProcessContext>` implementations that can be prioritized and conditionally applied.

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

Processors can expose **Customizers** to inject logic conditionally during Spring bootstrapping.