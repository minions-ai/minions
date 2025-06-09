# Core Concepts

Understanding the foundational elements of Minions is essential before diving into its deeper architecture. This section introduces the two most important constructs: **Agents** and **Recipes**.

---

### What is an Agent?

In Minions, an **Agent** is an autonomous entity capable of executing a goal-driven process. It engages in multi-step reasoning, decision-making, and tool interaction. Each agent operates within a defined context, maintaining state across steps and coordinating its behavior using recipes, memory, and step graphs.

#### Agent Lifecycle

1. **Initialization**: The agent is instantiated from a recipe. This includes loading its system prompt, goal, memory configuration, and step graph.
    
2. **Goal Assignment**: A goal is defined either at instantiation or injected dynamically (e.g., from user input or a higher-level planner).
    
3. **Step Execution**: The agent traverses its step graph, making decisions, calling models, and interacting with tools as needed.
    
4. **Memory Management**: The agent maintains multiple memory subsystems:
    - Episodic memory for chronological event storage
    - Vector memory for semantic similarity search
    - Short-term memory for active context
    - Long-term memory for persistent knowledge
    
5. **Memory Summarization**: After each step, the agent:
    - Queries relevant memory based on context
    - Generates summaries using model calls
    - Updates appropriate memory subsystems
    - Maintains memory efficiency through summarization
    
6. **Completion**: The agent completes once the graph reaches a terminal node or a completion condition is met.

Agents are **MCP-compliant** and **traceable**, allowing developers to monitor execution, review decision points, and integrate with observability tooling.

---

### What is a Recipe?

A **Recipe** is a declarative blueprint for constructing an agent. Rather than configuring agents imperatively through code, developers define recipes that describe the agent's identity, structure, and behavior.

A recipe includes:

- **System Prompt** – The initial set of instructions or framing context for the agent's behavior.
    
- **Goal** – The task or objective the agent is trying to achieve.
    
- **Step Graph** – A flow structure of logical steps the agent executes in order, conditionally, or iteratively.
    
- **Memory Configuration** – Defines how the agent manages memory:
    - Memory subsystem selection and configuration
    - Query strategy configuration
    - Summarization settings
    - Persistence strategy selection
    
- **Toolchain and Hooks** (optional) – Tools the agent can call and hooks for evaluation, logging, or transformation.

By encapsulating these into a recipe, Minions makes it easy to spin up different kinds of agents, each purpose-built for a use case, without rewriting orchestration logic.