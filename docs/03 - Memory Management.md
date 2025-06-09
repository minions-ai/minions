# Memory Management

Memory is a first-class concept in Minions. It gives agents the ability to remember context, recall facts, and summarize experiences across steps.

## Memory Subsystems

Minions implements a multi-tiered memory architecture:

- **Episodic Memory**: Stores sequences of events and experiences in chronological order
- **Vector Memory**: Stores semantic representations of messages for similarity-based retrieval
- **Short-Term Memory**: Maintains recent context and active working memory
- **Long-Term Memory**: Stores persistent knowledge and facts across conversations

## Memory Query Strategies

Memory querying is implemented through pluggable strategies that determine how to retrieve information:

### Step-based Strategies
- **StepVectorQueryStrategy**: Handles vector-based queries within step context
- **StepShortTermQueryStrategy**: Manages short-term memory queries for steps
- **StepEntityQueryStrategy**: Handles entity-based queries within step context

### Agent-based Strategies
- **AgentVectorQueryStrategy**: Manages vector-based queries at agent level
- **AgentShortTermQueryStrategy**: Handles short-term memory queries for agents
- **AgentLongTermQueryStrategy**: Manages long-term memory queries for agents
- **AgentEntityQueryStrategy**: Handles entity-based queries at agent level

## Memory Operations

The `MemoryManager` coordinates memory operations across subsystems:

```java
// Store messages in specific memory subsystem
memoryManager.store(MemorySubsystem.EPISODIC, message);

// Query memory using DSL
MemoryQuery query = MemoryQuery.builder()
    .expression(Expr.eq("conversationId", "C1"))
    .limit(10)
    .build();
List<Message> results = memoryManager.query(query);

// Retrieve specific message
Message message = memoryManager.retrieve("messageId");

// Flush memory
memoryManager.flush();
```

## Memory Persistence

Memory persistence is handled through pluggable strategies:

- **InMemoryPersistence**: Default strategy for development and testing
- **PostgresPersistence**: Production-ready persistence using PostgreSQL
- **Custom Persistence**: Implement `PersistenceStrategy` for custom storage

## Memory Summarization

The `MemorySummarizerProcessor` handles memory summarization:

1. Queries relevant memory based on conversation context
2. Uses model calls to generate summaries
3. Stores summaries in appropriate memory subsystems
4. Maintains memory efficiency by condensing older information

## Configuration

Memory behavior is configured through:

```java
SummarizationConfig config = SummarizationConfig.builder()
    .inputMessageLimit(10)
    .modelConfig(modelConfig)
    .build();
```

This allows fine-tuning of:
- Message limits for summarization
- Model selection for summarization
- Memory subsystem behavior
- Query strategy selection

## Memory Query System

The memory query system provides a flexible and expressive way to retrieve messages from memory subsystems. It consists of two main components: `MemoryQuery` and `MemoryQueryExpression`.

### MemoryQuery

`MemoryQuery` is the main query object for retrieving messages from memory. It supports:
- Specifying target memory subsystems
- Setting result limits
- Defining complex query expressions
- Backend-agnostic query construction

```java
MemoryQuery query = MemoryQuery.builder()
    .subsystems(MemorySubsystem.EPISODIC)  // Target specific memory subsystem
    .limit(10)                             // Limit results
    .expression(expression)                // Query expression
    .build();
```

### MemoryQueryExpression

`MemoryQueryExpression` is the base interface for all query expressions. It provides:
- A method to evaluate messages against the expression
- Support for logical operations (AND, OR, NOT)
- A fluent API for building complex expressions

The framework provides several built-in expression types:

1. **FieldEqualsExpression**
   - Matches exact field values
   ```java
   Expr.eq("role", MessageRole.USER)
   ```

2. **ContainsKeywordExpression**
   - Searches for keywords in text fields
   ```java
   Expr.contains("content", "keyword")
   ```

3. **RangeExpression**
   - Filters by time ranges
   ```java
   Expr.after("timestamp", Instant.now().minus(Duration.ofDays(1)))
   ```

4. **MetadataMatchExpression**
   - Matches metadata key-value pairs
   ```java
   Expr.metadata("priority", "high")
   ```

5. **VectorSimilarityExpression**
   - Performs vector similarity search
   ```java
   Expr.vector(embedding, topK)
   ```

6. **LogicalExpression**
   - Combines expressions with AND, OR, NOT
   ```java
   Expr.and(
       Expr.eq("role", MessageRole.USER),
       Expr.contains("content", "keyword")
   )
   ```

### Query Building

The `QueryBuilder` class provides a fluent API for constructing queries:

```java
MemoryQuery query = new QueryBuilder()
    .role(MessageRole.USER)
    .scope(MessageScope.CONVERSATION)
    .keyword("important")
    .after(Instant.now().minus(Duration.ofHours(1)))
    .entityType("user")
    .conversationId("conv123")
    .metadata(Map.of("priority", "high"))
    .build();
```

### Query Translation

Queries can be translated to different backend formats:

1. **MongoDB**
   ```java
   Query mongoQuery = mongoQueryTranslator.translate(memoryQuery);
   ```

2. **PostgreSQL**
   ```java
   String sql = postgresQueryTranslator.translate(memoryQuery);
   ```

3. **In-Memory**
   ```java
   List<Message> results = messages.stream()
       .filter(message -> expression.evaluate(message))
       .collect(Collectors.toList());
   ```

### Common Query Patterns

The `MemoryQueryUtils` class provides utility methods for common query patterns:

```java
// Get last N user messages
List<Message> userMessages = MemoryQueryUtils.getLastNUserMessages(
    memoryManager, 5, "conv123");

// Get last N assistant messages
List<Message> assistantMessages = MemoryQueryUtils.getLastNAssistantMessages(
    memoryManager, 5, "conv123");

// Get entities of specific type
List<Message> userEntities = MemoryQueryUtils.getEntitiesOfType(
    memoryManager, "user");
```

### Query Strategies

Query strategies implement the `MemoryQueryStrategy` interface to provide context-aware querying:

1. **Agent Strategies**
   - `AgentEntityQueryStrategy`: Queries entity memory
   - `AgentVectorQueryStrategy`: Performs vector similarity search
   - `AgentShortTermQueryStrategy`: Queries short-term memory
   - `AgentLongTermQueryStrategy`: Queries long-term memory

2. **Step Strategies**
   - `StepEntityQueryStrategy`: Queries entity memory
   - `StepVectorQueryStrategy`: Performs vector similarity search
   - `StepShortTermQueryStrategy`: Queries short-term memory

Each strategy:
- Implements `getMemoryQuery` to build appropriate queries
- Specifies supported memory subsystems
- Defines context acceptance criteria