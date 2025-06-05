# Extensibility & Error Handling

## Extending the Memory Subsystem
- **Add new memory types:** Implement MemoryDefinition and/or subclass AbstractMemory.
- **Add new strategies:** Implement MemoryPersistenceStrategy, MemoryQueryStrategy, or MemoryFlushStrategy.
- **Custom queries:** Extend the query DSL with new expression types.
- **Hybrid memory:** Use MemoryManager to compose multiple memories.

## Error Handling
- Use MessageNotFoundException for missing messages.
- Use MemoryResult for error tracking and result aggregation.

## Best Practices
- Use definitions and registries for configuration and dynamic lookup.
- Compose strategies and memories for advanced behaviors.
- Leverage the query DSL for backend-agnostic, expressive queries.
- Use context/result objects for extensibility and auditing. 