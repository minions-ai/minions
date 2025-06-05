# Definitions & Implementations

## MemoryDefinition
Defines the configuration and strategy selection for a memory subsystem. Specifies query, persistence, and flush strategies, and the memory's role and name. Used by factories to construct memory managers and chains.

## ShortTermMemory (Definition)
Example memory definition for short-term memory. Configures query, persistence, and flush strategies for short-term memory.

## impl.ShortTermMemory
Example concrete implementation of a short-term memory subsystem.

## MemoryDefinitionRegistry
Registry for all memory definitions. Maps MemorySubsystem to MemoryDefinition. Supports lookup and dynamic configuration.

### How to Define and Register a Memory
1. Implement a MemoryDefinition (e.g., ShortTermMemory) specifying strategies and role.
2. Register the definition with MemoryDefinitionRegistry.
3. Instantiate memory using buildMemory() with a MemoryStrategyRegistry and a MemoryPersistenceStrategy.

# Memory Definitions

The `definitions` subpackage contains classes for defining and configuring memory subsystems. These definitions encapsulate the configuration required to instantiate and register different types of memory (e.g., short-term, long-term) in the system.

## Main Class

- **ShortTermMemory**
  - Represents the definition/configuration for a short-term memory subsystem.
  - Specifies the memory role, strategies, and other parameters needed for instantiation.

## Usage

Memory definitions are used by factories and registries to create and configure memory subsystems at runtime, enabling flexible and declarative memory management. 