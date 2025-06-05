# Core API

## Memory<T extends Message>
Main interface for a memory subsystem. Defines methods for storing, retrieving, flushing, snapshotting, and querying messages. Identifies the memory subsystem type.

## AbstractMemory<T extends Message>
Abstract base class implementing Memory and Processor<MemoryContext>. Delegates to a pluggable MemoryPersistenceStrategy. Handles context processing and provides default implementations for snapshot/restore.

## DefaultMemory
Simple concrete implementation of AbstractMemory. Used as a generic memory type.

## MemoryManager
Orchestrates multiple memory subsystems. Maintains a chain of Processor<MemoryContext> instances. Delegates operations to all registered memories. Enables hybrid/composite memory management.

## MemoryContext
Context object for memory operations. Carries operation type, request details, and results. Extensible for custom metadata or advanced operations.

## MemoryRequest
Encapsulates the details of a memory operation (query, messages to store, message IDs to delete).

## MemoryResult<T extends Message>
Represents the outcome of a memory operation. Extends ProcessResult<T>. Used for result aggregation, auditing, and error tracking.

## MemoryDefinition
Defines configuration and strategy selection for a memory subsystem. Specifies query, persistence, and flush strategies, and the memory's role and name. Used by factories to construct memory managers and chains.

## MemoryDefinitionRegistry
Registry for all memory definitions. Maps MemorySubsystem to MemoryDefinition. Supports lookup and dynamic configuration.

## MessageNotFoundException
Exception thrown when a message is not found in memory.

## MemorySubsystem
Enum for memory subsystem types: SHORT_TERM, VECTOR, ENTITY, EPISODIC, MEMORY_MANAGER, LONG_TERM.

## OperationStatus & MemoryOperation
Enums for operation status (SUCCESS, FAILURE) and memory operation types (STORE, RETRIEVE, DELETE, QUERY, FLUSH).

## MemorySnapshot & InitiatorSnapshot
Support for snapshotting memory state. Extend for advanced persistence/rollback. 