Agents often rely on tools to gather data, make external API calls, or trigger workflows.

## Defining a Tool

A tool is a Spring Bean implementing `Tool<TInput, TOutput>`. It must define:

- The **name** and **input schema**
- Execution logic (usually async)
- Optionally: tool metadata and dynamic arguments

## Tool Usage Flow

1. Step issues a tool call request.
2. Tool resolves parameters and executes logic.
3. Output is passed to the agent and can be stored in memory or used for reasoning.