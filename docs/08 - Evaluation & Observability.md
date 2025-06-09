# Evaluation & Observability

Minions provides comprehensive evaluation and observability features, including memory-specific metrics and monitoring capabilities.

## Memory Metrics

### Performance Metrics

```java
@Component
public class MemoryMetrics {
    private final MeterRegistry registry;
    
    public void recordMemoryOperation(
            String operation,
            MemorySubsystem subsystem,
            long duration) {
        registry.timer("memory.operation", 
            "operation", operation,
            "subsystem", subsystem.name())
            .record(duration, TimeUnit.MILLISECONDS);
    }
}
```

### Memory Health

```java
@Component
public class MemoryHealthIndicator implements HealthIndicator {
    private final MemoryManager memoryManager;
    
    @Override
    public Health health() {
        return Health.builder()
            .withDetail("episodic", checkSubsystem(MemorySubsystem.EPISODIC))
            .withDetail("vector", checkSubsystem(MemorySubsystem.VECTOR))
            .build();
    }
}
```

## Memory Monitoring

### Query Monitoring

```java
@Aspect
@Component
public class MemoryQueryMonitor {
    @Around("execution(* com.minionslab.core.memory.MemoryManager.query(..))")
    public Object monitorQuery(ProceedingJoinPoint pjp) {
        // Monitor query performance
        // Track query patterns
        // Alert on anomalies
    }
}
```

### Storage Monitoring

```java
@Aspect
@Component
public class MemoryStorageMonitor {
    @Around("execution(* com.minionslab.core.memory.MemoryManager.store(..))")
    public Object monitorStorage(ProceedingJoinPoint pjp) {
        // Monitor storage operations
        // Track storage patterns
        // Alert on capacity issues
    }
}
```

## Memory Evaluation

### Query Evaluation

```java
@Component
public class MemoryQueryEvaluator {
    public QueryEvaluationResult evaluateQuery(
            MemoryQuery query,
            List<Message> results) {
        return QueryEvaluationResult.builder()
            .relevance(calculateRelevance(results))
            .completeness(calculateCompleteness(results))
            .build();
    }
}
```

### Summarization Evaluation

```java
@Component
public class MemorySummarizationEvaluator {
    public SummarizationEvaluationResult evaluateSummary(
            List<Message> original,
            Message summary) {
        return SummarizationEvaluationResult.builder()
            .coverage(calculateCoverage(original, summary))
            .accuracy(calculateAccuracy(original, summary))
            .build();
    }
}
```

## Observability Integration

### Prometheus Metrics

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Grafana Dashboards

```json
{
  "dashboard": {
    "panels": [
      {
        "title": "Memory Operations",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(memory_operation_seconds_count[5m])",
            "legendFormat": "{{operation}} - {{subsystem}}"
          }
        ]
      }
    ]
  }
}
```

## Testing Observability

```java
@Test
void testMemoryMetrics() {
    MemoryManager manager = new MemoryManager(memories, strategies);
    manager.store(MemorySubsystem.EPISODIC, message);
    
    // Verify metrics
    assertThat(registry.find("memory.operation")
        .tag("operation", "store")
        .tag("subsystem", "EPISODIC")
        .timer())
        .isNotNull();
}
```

Minions supports observability hooks throughout the agent lifecycle:

- Step-level evaluations
- Memory inspection
- Custom evaluators for response scoring or compliance
- Audit trails and trace IDs

Observers can be attached declaratively in recipes or dynamically at runtime.