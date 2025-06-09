

Memory is a first-class concept in Minions. It gives agents the ability to remember context, recall facts, and summarize experiences across steps.

## Memory Types

- **Chat Memory**: Stores ongoing interactions (user, assistant, system messages).
- **Step-local Memory**: Temporary memory scoped to a single step.
- **Long-Term Memory**: Structured storage for facts, retrieved through semantic or attribute-based queries.

## Memory Strategies

Memory behavior is pluggable via `MemoryStrategy` interfaces:

- **Append**: Simply add new messages to memory.
- **Windowed**: Keep only the latest N messages.
- **Summarize**: Condense older memory into summaries.
- **Flush and Rebuild**: Clear old memory and retain only key state.

You can define your own strategy by implementing `MemoryStrategy<T extends ProcessContext>` and wiring it via Spring.

## Memory Query DSL

Minions provides a DSL to express queries over memory:

```java
MemoryQuery query = MemoryQuery.builder()
    .expression(ExprUtil.getUserMessagesExpression("C1")
        .and(Expr.contains("content", "Hello"))
        .and(Expr.metadata("entityType", "testEntity")))
    .limit(10)
    .build();
```

This allows agents to retrieve only relevant subsets of memory for each step.