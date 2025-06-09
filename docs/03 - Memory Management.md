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