# Step Definitions

## StepDefinition
Base interface for all step definitions. Encapsulates the configuration and parameters for a step instance.

## AbstractStepDefinition
Abstract base class for common step definition logic. Used for code reuse and shared configuration.

## StepDefinitionType & StepDefinitionTypeIdResolver
- **StepDefinitionType:** Enum or marker for step definition types (e.g., MODEL_CALL, TOOL_CALL, BRANCH, etc.).
- **StepDefinitionTypeIdResolver:** Resolves type IDs for polymorphic (de)serialization and dynamic instantiation.

## StepDefinitionService
Service for managing, validating, and resolving step definitions. Handles registration, lookup, and lifecycle.

## StepDefinitionRegistry
Registry for all available step definitions. Supports dynamic registration and lookup by type.

## StepDefinitionConfig
Holds configuration for step definitions, such as default values and validation rules.

## StepDefinitionDeserializer
Custom deserializer for polymorphic step definitions (if needed).

## Concrete Step Definitions
- **AskUserStepDefinition**: Step for prompting the user.
- **SetEntityStepDefinition**: Step for setting an entity value.
- **SummarizeStepDefinition**: Step for summarization.
- **PlannerStepDefinition**: Step for planning logic.
- **BranchStepDefinition**: Step for conditional branching.
- **EvaluateStepDefinition**: Step for evaluation logic.
- **ModelCallStepDefinition**: Step for model (LLM) calls.
- **ToolCallStepDefinition**: Step for tool/integration calls.

## Extending with New Step Types
1. Implement StepDefinition and (optionally) AbstractStepDefinition.
2. Register the new type in StepDefinitionRegistry and StepFactory.
3. Provide a processor and implementation for the new step type. 