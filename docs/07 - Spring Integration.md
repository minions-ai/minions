# Spring Integration

Minions is built on Spring Boot, making it easy to integrate with existing Spring applications and leverage Spring's dependency injection and configuration capabilities.

## Memory Configuration

### Memory Subsystems

```java
@Configuration
public class MemoryConfig {
    @Bean
    public Memory<MemoryContext, Message> episodicMemory() {
        return new EpisodicMemory();
    }
    
    @Bean
    public Memory<MemoryContext, Message> vectorMemory() {
        return new VectorMemory();
    }
    
    @Bean
    public MemoryManager memoryManager(
            List<Memory<MemoryContext, Message>> memories,
            List<MemoryQueryStrategy> strategies) {
        return new MemoryManager(memories, strategies);
    }
}
```

### Query Strategies

```java
@Configuration
public class QueryStrategyConfig {
    @Bean
    public MemoryQueryStrategy stepVectorQueryStrategy() {
        return new StepVectorQueryStrategy();
    }
    
    @Bean
    public MemoryQueryStrategy agentLongTermQueryStrategy() {
        return new AgentLongTermQueryStrategy();
    }
}
```

### Persistence Configuration

```java
@Configuration
public class PersistenceConfig {
    @Bean
    public PersistenceStrategy postgresPersistenceStrategy(DataSource dataSource) {
        return new PostgresPersistenceStrategy(dataSource);
    }
}
```

## Auto-Configuration

Minions provides auto-configuration for common components:

```java
@Configuration
@ConditionalOnProperty(name = "minions.memory.enabled", havingValue = "true")
public class MemoryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public MemoryManager memoryManager() {
        // Default configuration
    }
}
```

## Custom Configuration

Override default configurations using Spring properties:

```yaml
minions:
  memory:
    enabled: true
    subsystems:
      episodic:
        enabled: true
      vector:
        enabled: true
    persistence:
      type: postgres
      schema: minions
```

## Testing Support

Spring provides testing utilities for memory components:

```java
@SpringBootTest
class MemoryIntegrationTest {
    @Autowired
    private MemoryManager memoryManager;
    
    @Test
    void testMemoryOperations() {
        // Test memory operations
    }
}
```

Minions is built around idiomatic Spring patterns:

- Use `@Component` to register custom steps, processors, tools, strategies
- `@ConfigurationProperties` for loading recipe or agent configuration from YAML or application context
- Conditional wiring using Spring Boot's auto-config features

You can fully extend Minions without modifying its internals, using standard Spring idioms.