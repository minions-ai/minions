# Step Graphs

## StepGraph
Interface for representing the structure and transitions of a workflow's steps. Supports dynamic, non-linear, and conditional flows.

## DefaultStepGraph
Default implementation of StepGraph. Supports standard transitions and step ordering.

## StepGraphDefinition & DefaultStepGraphDefinition
- **StepGraphDefinition:** Interface for defining the structure and transitions of a step graph.
- **DefaultStepGraphDefinition:** Default implementation for common workflows.

## StepGraphCompletionStrategy
Strategy for determining when a step graph is considered complete.

## NextStepTransitionStrategy & TransitionStrategy
- **NextStepTransitionStrategy:** Determines the next step to execute based on current context and outcome.
- **TransitionStrategy:** General interface for transition logic.

## StepGraphCustomizer
Interface for customizing step graph behavior and transitions.

## Customizing and Extending Step Graphs
- Implement custom StepGraph, StepGraphDefinition, or TransitionStrategy for advanced workflows.
- Use StepGraphCustomizer to inject custom logic or transitions. 