# Usage Patterns

## Defining and Running Steps
```java
StepDefinition def = new ModelCallStepDefinition(...);
Step step = stepFactory.create(def);
StepContext ctx = new StepContext(input);
step.execute(ctx);
```

## Customizing with Processors and Customizers
```java
StepProcessor customProcessor = new MyCustomProcessor();
DefaultStepProcessorChain chain = new DefaultStepProcessorChain(List.of(customProcessor, ...));
stepManager.setProcessorChain(chain);

StepCustomizer customizer = new MyStepCustomizer();
stepFactory.registerCustomizer(customizer);
```

## Building Dynamic Step Graphs
```java
StepGraphDefinition graphDef = new DefaultStepGraphDefinition(...);
StepGraph graph = new DefaultStepGraph(graphDef);
graph.execute(initialContext);
```

## Extending with New Types
- Implement new StepDefinition, Step, and StepProcessor.
- Register them in the appropriate registries/factories.
- Optionally, add customizers or graph logic for advanced workflows. 