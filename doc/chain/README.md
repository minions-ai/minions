# Chain Pattern in the Core Module

## Overview

The `chain` package in the `core` module implements a highly flexible and extensible **Chain of Responsibility** pattern. It is designed to support modular, pluggable, and dynamic processing pipelines for agent, step, and workflow orchestration. Chains are used to process context objects through a sequence of processors, each of which can handle, modify, or pass along the context.

## Main Components

### 1. `Chain<T extends Processor, R extends ProcessContext>`
- The main interface for a chain of processors.
- Supports adding/removing processors, dynamic ordering, and synchronous/asynchronous processing.
- Methods: `addToStart`, `addToEnd`, `addBefore`, `addAfter`, `remove`, `getProcessors`, `process`, `processAsync`, `accepts`.

### 2. `AbstractBaseChain<T extends Processor, R extends ProcessContext>`
- Base implementation of the `Chain` interface.
- Manages processor lists, applies customizers, and implements the main processing loop.
- Subclasses implement `registerProcessors()` to define the initial chain.
- Handles before/after/error hooks for each processor.

### 3. `Processor<T extends ProcessContext>`
- Interface for a unit of work in the chain.
- Methods: `accepts`, `process`, `processAsync`, `beforeProcess`, `afterProcess`, `onError`, `getPriority`, `getDescription`.
- Can be extended for custom logic, error handling, and hooks.

### 4. `ProcessContext<T extends ProcessResult>`
- The context object passed through the chain.
- Accumulates results, metadata, and state.
- Extensible for domain-specific needs.

### 5. `ProcessResult<T>`
- Represents the outcome of processing a context through a processor or chain.
- Captures processor ID, status, error, timing, and sub-results.
- Factory methods for success, skipped, and failure results.

### 6. `ChainRegistry`
- Manages named chains, supports registration, lookup, and dynamic selection of chains based on context.
- Applies `ChainCustomizer`s and `ProcessorCustomizer`s for advanced configuration.

### 7. Customizers
- `ChainCustomizer`: Customizes entire chains (e.g., for logging, metrics, conditional logic).
- `ProcessorCustomizer`: Customizes individual processors before they are added to a chain.

### 8. `AbstractProcessor<T extends ProcessContext, C>`
- Base class for processors with a standard process flow and result handling.

### 9. `ChainDefinition`
- Interface for defining and building named chains.

## How to Define and Use a Chain

1. **Define Processors:** Implement the `Processor` interface for each unit of work.
2. **Create a Chain:** Subclass `AbstractBaseChain` and implement `registerProcessors()` to add processors in the desired order.
3. **Register Chains:** Use `ChainRegistry` to register and manage chains by name.
4. **Process Contexts:** Call `process(context)` or `processAsync(context)` on a chain to execute the pipeline.
5. **Customize:** Add `ChainCustomizer` and `ProcessorCustomizer` beans for cross-cutting concerns.

## Extensibility and Customization
- **Dynamic Composition:** Chains and processors can be registered, removed, or reordered at runtime.
- **Custom Chains:** Subclass `AbstractBaseChain` for custom pipelines.
- **Custom Processors:** Implement `Processor` for new logic.
- **Custom Contexts:** Extend `ProcessContext` for domain-specific state.
- **Customizers:** Add cross-cutting concerns (logging, metrics, etc.) via customizer interfaces.

## Nuances and Advanced Features
- **Dynamic Registration:** Chains and processors can be registered/unregistered at runtime.
- **Customizers:** Both chains and processors can be decorated or modified by customizers.
- **Error Handling:** Each processor can handle errors via `onError`.
- **Async Processing:** Chains and processors support asynchronous execution via `processAsync`.
- **Result Aggregation:** Results are accumulated in the context for auditing, debugging, or further processing.
- **Chain Selection:** `ChainRegistry` can select the appropriate chain for a given context type.
- **Spring Integration:** Designed for use with Spring's dependency injection and bean lifecycle.

## Example Usage

```java
// Define a processor
public class LoggingProcessor implements Processor<MyContext> {
    public boolean accepts(MyContext ctx) { return true; }
    public MyContext process(MyContext ctx) {
        System.out.println("Processing: " + ctx);
        return ctx;
    }
}

// Define a chain
public class MyChain extends AbstractBaseChain<Processor<MyContext>, MyContext> {
    @Override
    protected void registerProcessors() {
        this.addToStart(new LoggingProcessor());
        // Add more processors as needed
    }
    @Override
    public boolean accepts(ProcessContext ctx) {
        return ctx instanceof MyContext;
    }
}

// Register and use the chain
ChainRegistry registry = ...;
MyChain chain = new MyChain();
registry.register("myChain", chain);
MyContext context = new MyContext();
registry.process(context);
```

## Diagrams
- [Class Diagram](./chain_class_diagram.mmd)
- [Sequence Diagram](./chain_sequence_diagram.mmd)

---

See the diagrams and source code for further details and advanced usage patterns. 