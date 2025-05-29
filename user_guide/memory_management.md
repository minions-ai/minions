# Memory Management in Minions

## Overview

The Minions agent framework provides a modular, extensible memory management system designed for both simple and advanced use cases. Memory is organized into subsystems, each with a clear responsibility, and can be managed, flushed, and persisted independently or together.

### Memory Subsystems

| Subsystem      | Purpose/Typical Use Case                |
|---------------|-----------------------------------------|
| SHORT_TERM    | Recent chat history, working memory     |
| ENTITY        | Facts, user profiles, named entities    |
| VECTOR        | Embeddings for retrieval-augmented tasks|
| EPISODIC      | Temporary or session-based context      |

Each subsystem is accessed via the `MemoryManager` interface, which provides a unified API for querying, storing, and managing memory.

---

## Default (Simple) Usage

Out of the box, Minions uses in-memory implementations for all subsystems. This means:

- **All memory is stored in RAM** and is lost when the process stops.
- **No configuration is required** for basic use.
- **All subsystems are available** and managed by a `DefaultMemoryManager`.

### Example: Using the Default MemoryManager

```java
MemoryManager memoryManager = new DefaultMemoryManager();

// Store a message
memoryManager.getShortTermMemory().store(message);

// Query recent user messages
List<Object> recent = memoryManager.query(MemoryQuery.builder()
    .subsystems(Set.of(MemoryQuery.MemorySubsystem.SHORT_TERM))
    .role(MessageRole.USER)
    .limit(5)
    .build());
```

---

## Flushing and Persisting Memory

### Flushing
- **Flushing** means clearing memory (removing all or some data).
- By default, you can flush all memory subsystems at once:

```java
memoryManager.flush(); // Flushes all subsystems
```
- Or flush a specific subsystem:
```java
memoryManager.flush(MemoryQuery.MemorySubsystem.SHORT_TERM); // Only short-term memory
```

### Persistence
- **Persistence** means saving memory so it can be restored later (e.g., after a restart).
- By default, in-memory persistence is a snapshot (RAM only):

```java
memoryManager.persist(); // Persist all subsystems (snapshot in RAM)
```
- Or persist a specific subsystem:
```java
memoryManager.persist(MemoryQuery.MemorySubsystem.ENTITY);
```
- To restore:
```java
memoryManager.restore(); // Restore all subsystems
memoryManager.restore(MemoryQuery.MemorySubsystem.ENTITY); // Restore only entity memory
```

---

## Customization: Strategies

You can register custom strategies to control how and when memory is flushed or persisted. This is useful for:
- Using a database or file for persistence
- Flushing only certain types of data
- Implementing TTL (time-to-live) or size-based policies

### Flushing Strategies
A `MemoryFlushingStrategy` decides if it applies to a subsystem and how to flush it.

```java
public interface MemoryFlushingStrategy {
    boolean accepts(MemoryQuery.MemorySubsystem subsystem);
    void flush(MemoryManager memoryManager, MemoryQuery.MemorySubsystem subsystem);
}
```

#### Registering a Custom Flushing Strategy
```java
memoryManager.registerFlushingStrategy(new MyCustomFlushingStrategy());
```

### Persistence Strategies
A `MemoryPersistenceStrategy` decides if it applies to a subsystem and how to persist/restore it.

```java
public interface MemoryPersistenceStrategy {
    boolean accepts(MemoryQuery.MemorySubsystem subsystem);
    void persist(MemoryManager memoryManager, MemoryQuery.MemorySubsystem subsystem);
    void restore(MemoryManager memoryManager, MemoryQuery.MemorySubsystem subsystem);
}
```

#### Registering a Custom Persistence Strategy
```java
memoryManager.registerPersistenceStrategy(new MyCustomPersistenceStrategy());
```

---

## Example: Custom Strategy for Only Short-Term Memory

```java
public class ShortTermOnlyFlushingStrategy implements MemoryFlushingStrategy {
    @Override
    public boolean accepts(MemoryQuery.MemorySubsystem subsystem) {
        return subsystem == MemoryQuery.MemorySubsystem.SHORT_TERM;
    }
    @Override
    public void flush(MemoryManager memoryManager, MemoryQuery.MemorySubsystem subsystem) {
        memoryManager.getShortTermMemory().clear();
    }
}
```

---

## Implications of Different Strategies

| Strategy Type | Example Use Case | Pros | Cons |
|--------------|------------------|------|------|
| In-memory (default) | Fast prototyping, tests | Simple, fast | Data lost on restart |
| File/DB persistence | Production, audit | Durable, recoverable | More setup, slower |
| Selective/TTL flush | Privacy, resource mgmt | Fine control | More logic needed |

- **Multiple strategies can be registered**; only those that `accept` a subsystem will be used for that subsystem.
- **Order of registration matters** if strategies have side effects.

---

## Best Practices

- **Start with the defaults** for rapid prototyping and development.
- **Add custom strategies** only when you need persistence, selective flushing, or advanced policies.
- **Test your strategies** to ensure data is managed as expected.
- **Document your memory policies** for maintainability.

---

## Extensibility for Advanced Users

- Implement your own strategies for any subsystem or context.
- Combine multiple strategies for complex policies (e.g., TTL + DB persistence).
- Use the `accepts` method to target only relevant subsystems.
- You can extend the `MemoryManager` for even more control if needed.

---

## Quick Reference: Subsystems

| Subsystem   | Typical Use Case                |
|-------------|---------------------------------|
| SHORT_TERM  | Chat history, working memory    |
| ENTITY      | Facts, user profiles            |
| VECTOR      | Embeddings for retrieval        |
| EPISODIC    | Temporary/session context       |

---

For more details, see the API docs or explore the source code in the `core.memory` package. 