# Memory Strategies

The `strategy` subpackage provides the pluggable strategy interfaces and implementations for the memory subsystem. It enables flexible selection and composition of persistence, query, and flush strategies, supporting multiple backends and custom logic.

## Main Interfaces and Classes

- **MemoryPersistenceStrategy**
  - Interface for persistence backends (in-memory, MongoDB, PostgreSQL, etc.).
  - Defines CRUD operations, candidate fetching, and type-specific queries.

- **MemoryQueryStrategy**
  - Interface for pluggable query strategies (e.g., smart, hybrid, or custom query logic).

- **MemoryFlushStrategy**
  - Interface for strategies that control when and how memory is flushed or persisted.

- **MemoryStrategyRegistry**
  - Registry for all available strategies, supporting lookup and dynamic selection.

- **NoOpStrategy, MemoryStrategy, PersistenceAdapter, MemoryItem**
  - Utility and base interfaces/classes for strategy composition and extension.

## Submodules

### `strategy/persistence`
- **InMemoryPersistenceStrategy**: In-memory backend using a concurrent map.
- **MongoPersistenceStrategy**: MongoDB backend using Spring Data MongoDB.
- **PostgresPersistenceStrategy**: PostgreSQL backend using JDBC.
- **Mappers/Translators**: Classes for mapping between domain objects and backend representations (e.g., `PostgresMessageMapper`, `MessageMongoMapper`, `PostgresQueryTranslator`, `MongoQueryTranslator`).

### `strategy/query`
- **AbstractSmartMemoryQueryStrategy, AgentSmartMemoryQueryStrategy**: Advanced query strategies for context-aware or agent-specific querying.

### `strategy/flush`
- (Currently empty, but designed for future flush strategy implementations.)

## Usage

Strategies are selected and composed at runtime based on memory definitions and configuration, enabling backend-agnostic, extensible memory management. 