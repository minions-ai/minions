# Step Processors

## StepProcessor
Interface for processing steps. Defines the contract for handling step execution logic. Each step type typically has a corresponding processor.

## DefaultStepProcessorChain
Implements a chain of responsibility for step processors. Allows multiple processors to be composed and invoked in sequence for a single step.

## Concrete Processors
- **PreparationProcessor**: Handles pre-processing and setup for steps.
- **StepCompletionProcessor**: Handles completion logic and output for steps.
- **PlannerStepProcessor**: Processor for planning steps.
- **SummarizeStepProcessor**: Processor for summarization steps.
- **ModelCallStepProcessor**: Processor for model (LLM) call steps.
- **ToolCallStepProcessor**: Processor for tool/integration call steps.

## Chaining and Customization
- Processors can be chained using DefaultStepProcessorChain for complex, multi-stage processing.
- Custom processors can be added by implementing StepProcessor and registering them in the chain.

## Extending with New Processors
1. Implement StepProcessor for the new logic.
2. Register the processor in the processor chain or StepManager.
3. Optionally, provide custom chaining or ordering logic. 