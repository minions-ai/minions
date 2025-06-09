
A **Step Graph** defines the structured flow of logic an agent follows to reach its goal. Unlike ad hoc prompt chaining, Minions formalizes execution paths using a graph of steps connected by transitions.

## Components

- **Step**: A unit of logic or decision. A step can invoke a model, execute a tool call, or make a transition decision.
- **StepGraphDefinition**: Declarative definition of steps and their transitions.
- **TransitionStrategy**: Defines how the agent determines the next step. Can be sequential, conditional, or dynamic.
- **Start Step / Terminal Step**: Every graph has an entry point and can define one or more terminal states.

## Step Execution Flow

1. Agent starts at the defined start step.
2. Executes step logic: e.g., prompts the model, triggers a tool, processes memory.
3. Determines the next step using a transition strategy.
4. Repeats until a terminal step is reached or a completion condition is satisfied.

## Customizing Step Graphs

Minions supports a Definition/Customizer pattern:

- **Definition** defines the logic and structure.
- **Customizers** allow injection of additional behavior, transitions, or overrides without modifying the core.

This allows default agent flows to be extended in context-aware ways—e.g., adding a retry step only in certain environments.