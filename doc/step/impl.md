# Step Implementations

## AbstractStep
Base class for step implementations. Provides common logic for input/output handling, status management, and context integration.

## Concrete Step Implementations
- **ModelCallStep**: Executes a model (LLM) call.
- **EvaluateStep**: Performs evaluation logic.
- **BranchStep**: Handles conditional branching.
- **SetEntityStep**: Sets an entity value.
- **AskUserStep**: Prompts the user for input.
- **SummarizeStep**: Performs summarization.
- **PlannerStep**: Executes planning logic.
- **LoopStep**: Handles loop constructs.
- **ToolCallStep**: Executes a tool/integration call.

## Relationship to Definitions and Processors
- Each implementation corresponds to a StepDefinition and is executed by a StepProcessor.
- StepFactory creates implementations from definitions.

## Adding New Step Implementations
1. Subclass AbstractStep and implement required logic.
2. Register the implementation in StepFactory and provide a corresponding StepDefinition and StepProcessor. 