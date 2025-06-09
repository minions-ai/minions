# Testing & Debugging

## Unit Testing

### Testing Memory Components

#### Testing Memory Query Strategies

```java
@ExtendWith(MockitoExtension.class)
class StepVectorQueryStrategyTest {
    @Mock
    private StepContext stepContext;
    
    @BeforeEach
    void setUp() {
        strategy = new StepVectorQueryStrategy();
    }
    
    @Test
    void testGetMemoryQueryReturnsValidQuery() {
        MemoryQuery query = strategy.getMemoryQuery(stepContext);
        assertNotNull(query);
        assertNotNull(query.getExpression());
    }
    
    @Test
    void testAcceptsStepContext() {
        assertTrue(strategy.accepts(stepContext));
        assertFalse(strategy.accepts(mock(ProcessContext.class)));
    }
}
```

#### Testing Memory Summarization

```java
@ExtendWith(MockitoExtension.class)
class MemorySummarizerProcessorTest {
    @Mock
    private AgentContext agentContext;
    @Mock
    private MemoryManager memoryManager;
    @Mock
    private ModelCallService modelCallService;
    
    @BeforeEach
    void setUp() {
        processor = new MemorySummarizerProcessor(modelCallService);
        when(agentContext.getMemoryManager()).thenReturn(memoryManager);
    }
    
    @Test
    void testDoProcessQueriesMemoryAndStoresMessages() {
        List<Message> memoryMessages = List.of(message1, message2);
        when(memoryManager.query(any(MemoryQuery.class))).thenReturn(memoryMessages);
        
        processor.process(agentContext);
        
        verify(memoryManager).query(any(MemoryQuery.class));
        verify(memoryManager).storeAll(anyList(), eq(MemorySubsystem.EPISODIC));
        verify(memoryManager).storeAll(anyList(), eq(MemorySubsystem.VECTOR));
    }
}
```

### Testing Memory Persistence

```java
@Test
void testPostgresPersistence() {
    PersistenceStrategy strategy = new PostgresPersistenceStrategy(dataSource);
    Message message = createTestMessage();
    
    strategy.store(message);
    Message retrieved = strategy.retrieve(message.getId());
    
    assertEquals(message.getId(), retrieved.getId());
    assertEquals(message.getContent(), retrieved.getContent());
}
```

## Integration Testing

### Testing Memory Chain

```java
@Test
void testMemoryChain() {
    MemoryManager manager = new MemoryManager(memories, queryStrategies);
    Message message = createTestMessage();
    
    manager.store(MemorySubsystem.EPISODIC, message);
    Message retrieved = manager.retrieve(message.getId());
    
    assertNotNull(retrieved);
    assertEquals(message.getId(), retrieved.getId());
}
```

## Debugging Memory Issues

1. **Memory Query Issues**:
   - Check query expressions
   - Verify memory subsystem configuration
   - Inspect query strategy selection

2. **Summarization Issues**:
   - Verify model call configuration
   - Check input message limits
   - Inspect summary storage

3. **Persistence Issues**:
   - Check database connectivity
   - Verify schema configuration
   - Inspect transaction handling