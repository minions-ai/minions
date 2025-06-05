# Usage Patterns

## Storing and Retrieving Messages
```java
Memory<Message> memory = ...;
memory.store(message);
Message retrieved = memory.retrieve(messageId);
```

## Querying Memory
```java
MemoryQuery query = MemoryQuery.builder()
    .limit(10)
    .queryBuilder(new QueryBuilder().role(MessageRole.USER).keyword("hello"))
    .build();
List<Message> results = memory.query(query);
```

## Using MemoryManager for Hybrid Memory
```java
MemoryManager manager = new MemoryManager(List.of(shortTermMemory, longTermMemory));
manager.store(message);
List<Message> allResults = manager.query(query);
```

## Advanced Querying with Expressions
```java
MemoryQueryExpression expr = Expr.and(
    Expr.eq("role", MessageRole.USER),
    Expr.contains("content", "keyword"),
    Expr.after("timestamp", Instant.now().minus(Duration.ofDays(1)))
);
MemoryQuery query = MemoryQuery.builder().expression(expr).build();
List<Message> results = memory.query(query);
```

## Error Handling
- Use MessageNotFoundException for missing messages.
- Use MemoryResult for error tracking and result aggregation. 