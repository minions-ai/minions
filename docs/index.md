**Minions** is an extensible, MCP-compliant agentic AI framework written in Java and built using idiomatic Spring practices. Designed from the ground up with the principles of **extensibility**, **traceability**, and **enterprise readiness**, Minions provides a solid foundation for building and orchestrating autonomous AI agents in regulated and complex environments.

At its core, Minions follows the **Open/Closed Principle**: the system is open for extension but closed for modification. Whether you're building a ReAct-style reasoning agent, a planner-executor-observer pipeline, or a bespoke recipe for your enterprise workflow, Minions enables it without changing the underlying framework.

##  Why Minions?

The AI ecosystem has rapidly shifted toward **agentic workflows**—systems where autonomous agents reason, plan, act, and reflect over multiple steps. While many open-source frameworks offer this in Python, Minions is one of the few that brings this capability to the **enterprise Java ecosystem** with full support for:

- **Spring Boot auto-configuration and injection**
- **Modular architecture** that can be expanded with Spring Beans
- **Privacy and governance by design**
- **Idiomatic support for lifecycle management, configuration, and testing**

##  Key Design Principles

- **Extensibility First**  
  Minions uses design patterns like *Definition/Customizer*, *Chain of Responsibility*, and *Factory Registry* to allow seamless plug-in of new components—steps, tools, memories, processors—without modifying the core.

- **Idiomatic Spring Integration**  
  The framework follows familiar Spring idioms, making it easy for developers to inject, override, and extend functionality using standard annotations and configuration mechanisms.

- **MCP Compliance**  
  Minions adheres to the [Model Connector Protocol (MCP)](https://github.com/langchain-ai/langgraph/blob/main/spec/mcp.md), enabling composability, cross-model interoperability, and architectural clarity.

- **Enterprise-Grade Capabilities**  
  Features like memory privacy, audit hooks, multi-tenant memory isolation, and structured evaluation make Minions a suitable foundation for regulated industries like insurance, healthcare, and finance.

##  Agent Recipes

Every agent in Minions is instantiated from a **Recipe**. A recipe defines the agent’s purpose and structure:

- Its **system prompt** and **goal**
- The **step graph** that outlines its thought/action flow
- Its **memory strategy**, specifying what the agent remembers and when
- Optional toolchains and observers

Different types of recipes are available out-of-the-box, including:
- `ReActAgentRecipe`
- `PlannerExecutorObserverRecipe`
- And more custom agent types

We’ll dive deeper into recipes, step graphs, and memory strategies in the following sections.