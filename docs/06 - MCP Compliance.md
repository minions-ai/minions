# MCP Compliance

Minions is designed to be compatible with the [Model Connector Protocol (MCP)](https://github.com/langchain-ai/langgraph/blob/main/spec/mcp.md), with ongoing work to achieve full compliance.

## Current Implementation

### Memory System

The memory system is designed with MCP compatibility in mind:

```java
// Memory query with standard fields
MemoryQuery query = MemoryQuery.builder()
    .subsystems(MemorySubsystem.EPISODIC)
    .limit(10)
    .expression(Expr.eq("conversationId", "C1"))
    .build();

// Message with standard metadata
Message message = Message.builder()
    .content("content")
    .metadata("type", "user_message")
    .build();
```

### Memory Subsystems

Memory subsystems follow a modular design that can be extended for MCP compliance:

```java
public class EpisodicMemory implements Memory<MemoryContext, Message> {
    @Override
    public MemorySubsystem getMemorySubsystem() {
        return MemorySubsystem.EPISODIC;
    }
}
```

## Planned MCP Compliance

1. **Memory Operations**
   - [ ] Implement MCP-compliant expression types
   - [ ] Add MCP metadata handling
   - [ ] Support MCP-specific query patterns

2. **Memory Subsystems**
   - [ ] Add MCP interface implementations
   - [ ] Implement MCP metadata providers
   - [ ] Support MCP-specific operations

3. **Memory Persistence**
   - [ ] Add MCP metadata storage
   - [ ] Support MCP data formats
   - [ ] Implement MCP query language support
