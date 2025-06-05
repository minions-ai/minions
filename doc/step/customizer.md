# Customizers & Extensibility

## StepCustomizer
Interface for customizing step behavior, input, output, or execution logic. Used to inject cross-cutting or dynamic behavior into steps.

## ModelCallStepCustomizer
Example customizer for model call steps. Modifies or augments model call behavior.

## AbstractStepCustomizer
Base class for customizers, providing common logic and extension points.

## Using Customizers
- Register customizers with StepFactory, StepManager, or directly with steps.
- Use customizers to inject dynamic parameters, modify execution, or add cross-cutting logic (e.g., logging, validation).

## Adding New Customizers
1. Implement StepCustomizer or extend AbstractStepCustomizer.
2. Register the customizer with the appropriate step or manager.
3. Optionally, provide configuration or dynamic registration logic. 