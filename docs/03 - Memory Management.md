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

`MemoryQuery` is the main query object for retrieving messages from memory. It is implemented using Lombok annotations for builder pattern and data access:

```java
@Data
@Accessors(chain = true)
@EqualsAndHashCode
@AllArgsConstructor
@Builder
@ToString
public class MemoryQuery {
    private MemorySubsystem subsystems;
    private int limit;
    private MemoryQueryExpression expression;
    
    public MemoryQuery(@NotBlank MemorySubsystem subsystem, int limit) {
        this.subsystems = subsystem;
        this.limit = limit;
    }
}
```

### MemoryQueryExpression

`MemoryQueryExpression` is the base interface for all query expressions. It provides:
- A method to evaluate messages against the expression
- Default methods for logical operations (AND, OR, NOT)
- Support for expression composition

```java
public interface MemoryQueryExpression {
    boolean evaluate(Message message);
    
    default MemoryQueryExpression and(MemoryQueryExpression... expressions) {
        List<MemoryQueryExpression> all = new ArrayList<>();
        all.add(this);
        Collections.addAll(all, expressions);
        return new LogicalExpression(LogicalOperator.AND, all);
    }
    
    // Similar default methods for or() and not()
}
```

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
   - Performs vector similarity search (placeholder implementation)
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

7. **AlwaysTrueExpression**
   - Default expression that always evaluates to true
   ```java
   Expr.alwaysTrue()
   ```

### Query Building

The `QueryBuilder` class provides a fluent API for constructing queries, with some methods returning expressions directly:

```java
// Returns MemoryQueryExpression
MemoryQueryExpression roleExpr = new QueryBuilder()
    .role(MessageRole.USER);

// Returns QueryBuilder for chaining
MemoryQuery query = new QueryBuilder()
    .scope(MessageScope.CONVERSATION)
    .keyword("important")
    .after(Instant.now().minus(Duration.ofHours(1)))
    .entityType("user")
    .conversationId("conv123")
    .metadata(Map.of("priority", "high"))
    .build();
```

### Query Translation

Queries can be translated to different backend formats with parameter binding:

1. **MongoDB**
   ```java
   Query mongoQuery = mongoQueryTranslator.translate(memoryQuery);
   // Translates to Spring Data MongoDB Query with Criteria
   ```

2. **PostgreSQL**
   ```java
   String sql = postgresQueryTranslator.translate(memoryQuery);
   // Generates parameterized SQL with ? placeholders
   ```

3. **In-Memory**
   ```java
   List<Message> results = messages.stream()
       .filter(message -> expression.evaluate(message))
       .collect(Collectors.toList());
   ```

### Common Query Patterns

The `MemoryQueryUtils` class provides utility methods for common query patterns, with type filtering:

```java
// Get last N user messages with type filtering
List<Message> userMessages = MemoryQueryUtils.getLastNUserMessages(
    memoryManager, 5, "conv123")
    .stream()
    .filter(Message.class::isInstance)
    .map(message -> message)
    .collect(Collectors.toList());

// Get last N assistant messages with subsystem specification
List<Message> assistantMessages = MemoryQueryUtils.getLastNAssistantMessages(
    memoryManager, 5, "conv123");

// Get entities of specific type with subsystem specification
List<Message> userEntities = MemoryQueryUtils.getEntitiesOfType(
    memoryManager, "user");
```

### Query Strategies

Query strategies implement the `MemoryQueryStrategy` interface to provide context-aware querying:

1. **Agent Strategies**
   - `AgentEntityQueryStrategy`: Queries entity memory with `AlwaysTrueExpression`
   - `AgentVectorQueryStrategy`: Performs vector similarity search
   - `AgentShortTermQueryStrategy`: Queries short-term memory using `ExprUtil`
   - `AgentLongTermQueryStrategy`: Queries long-term memory with recipe ID

2. **Step Strategies**
   - `StepEntityQueryStrategy`: Queries entity memory with `AlwaysTrueExpression`
   - `StepVectorQueryStrategy`: Performs vector similarity search
   - `StepShortTermQueryStrategy`: Queries short-term memory using `ExprUtil`

Each strategy:
- Implements `getMemoryQuery` to build appropriate queries using `QueryConfig`
- Specifies supported memory subsystems
- Defines context acceptance criteria
- Uses `MemoryManager`'s `queryStrategies` for execution