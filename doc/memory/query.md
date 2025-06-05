# Memory Query

The `query` subpackage provides the query infrastructure for the memory subsystem, enabling expressive and flexible querying of memory contents. It includes query objects, builders, utilities, and a rich expression tree for advanced filtering.

## Main Classes

- **MemoryQuery**
  - The main query object for retrieving messages and memory items.
  - Supports specifying filters, limits, and subsystems.

- **QueryBuilder**
  - Fluent builder for constructing `MemoryQuery` instances and complex query expressions.
  - Supports chaining filters for role, scope, keyword, entity type, metadata, and more.

- **MemoryQueryUtils**
  - Utility methods for common query patterns and operations.

- **Queryable**
  - Interface for objects that can be queried.

- **QueryConfig**
  - Configuration object for query parameters and defaults.

## Expression Subpackage (`query/expression`)

- **Expr**
  - Factory for building query expressions (e.g., field equals, contains, range, logical AND/OR/NOT).
- **MemoryQueryExpression**
  - Base interface for all query expressions.
- **FieldEqualsExpression, ContainsKeywordExpression, RangeExpression, MetadataMatchExpression, LogicalExpression, AlwaysTrueExpression, VectorSimilarityExpression**
  - Concrete expression types for building complex, composable queries.

## Usage

The query subsystem allows for:
- Filtering by message fields, metadata, time ranges, and more.
- Composing complex queries using logical operators.
- Backend-agnostic query construction, with translation to SQL, MongoDB, or in-memory filtering as needed.

# Query System

## MemoryQuery
Query object for retrieving messages from memory. Supports flexible, backend-agnostic queries. Fields: subsystems, limit, QueryBuilder, MemoryQueryExpression.

## QueryBuilder
Fluent builder for constructing MemoryQuery instances and complex query expressions. Methods: role, scope, keyword, after, before, entityType, conversationId, metadata, id, build.

## MemoryQueryUtils
Utility methods for common query patterns (e.g., get last N user messages, get entities by type, etc.).

## Query Expressions
- **Expr:** Factory for building query expressions (field equals, contains, range, logical AND/OR/NOT, etc.).
- **MemoryQueryExpression:** Base interface for all query expressions.
- **FieldEqualsExpression, ContainsKeywordExpression, RangeExpression, MetadataMatchExpression, LogicalExpression, AlwaysTrueExpression, VectorSimilarityExpression:** Concrete expression types for building complex, composable queries.

### Example: Advanced Querying
```java
MemoryQueryExpression expr = Expr.and(
    Expr.eq("role", MessageRole.USER),
    Expr.contains("content", "keyword"),
    Expr.after("timestamp", Instant.now().minus(Duration.ofDays(1)))
);
MemoryQuery query = MemoryQuery.builder().expression(expr).build();
List<Message> results = memory.query(query);
``` 