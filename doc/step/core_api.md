# Core API

## Step
Represents a single unit of work in a workflow. Defines the contract for execution, context management, and status tracking. Supports custom input/output and extensible processing.

## StepContext
Carries all contextual information for step execution, including input, output, status, errors, and metadata. Used throughout the step lifecycle.

## StepStatus
Enum representing the status of a step (e.g., PENDING, RUNNING, COMPLETED, FAILED, ABORTED).

## StepException
Custom exception for step execution errors. Used for error handling and propagation.

## StepFactory
Factory for creating Step instances from StepDefinition objects. Supports dynamic instantiation and registration of new step types.

## StepService & StepServiceImpl
Service interface and implementation for managing step execution, orchestration, and lifecycle. Provides methods for running, resuming, and managing steps.

## StepManager
Orchestrates step execution, manages step chains, and coordinates with processors and completion logic.

## StepCompletionOutputInstructions
Encapsulates output instructions for step completion, including next step transitions and output data. 