# MCP Compliance

Minions adheres to the [Model Connector Protocol (MCP)](https://github.com/langchain-ai/langgraph/blob/main/spec/mcp.md), ensuring compatibility with other MCP-compliant systems and tools.

## Memory Compliance

### Memory Operations

All memory operations follow MCP specifications:

```java
// MCP-compliant memory query
MemoryQuery query = MemoryQuery.builder()
    .expression(Expr.mcp("conversationId", "C1"))
    .limit(10)
    .build();

// MCP-compliant memory storage
Message message = Message.builder()
    .content("content")
    .metadata("mcp:type", "user_message")
    .build();
```

### Memory Subsystems

Each memory subsystem implements MCP interfaces:

```java
public class EpisodicMemory implements Memory<MemoryContext, Message>, McpCompliant {
    @Override
    public McpMetadata getMcpMetadata() {
        return McpMetadata.builder()
            .type("episodic")
            .version("1.0")
            .build();
    }
}
```

## Compliance Requirements

1. **Memory Operations**
   - All memory queries must use MCP-compliant expressions
   - Memory storage must include MCP metadata
   - Memory retrieval must respect MCP limits

2. **Memory Subsystems**
   - Each subsystem must implement McpCompliant interface
   - Subsystems must provide MCP metadata
   - Subsystems must handle MCP-specific operations

3. **Memory Persistence**
   - Persistence strategies must store MCP metadata
   - Data formats must follow MCP specifications
   - Query languages must support MCP expressions

## Testing Compliance

```java
@Test
void testMcpCompliance() {
    MemoryManager manager = new MemoryManager(memories, strategies);
    
    // Test MCP-compliant query
    MemoryQuery query = createMcpQuery();
    List<Message> results = manager.query(query);
    assertMcpCompliant(results);
    
    // Test MCP-compliant storage
    Message message = createMcpMessage();
    manager.store(MemorySubsystem.EPISODIC, message);
    assertMcpCompliant(message);
}
```
