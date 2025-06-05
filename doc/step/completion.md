# Step Completion

## StepCompletionChain
Chain of processors for handling step completion logic. Allows for modular, extensible completion handling.

## StepCompletionContext
Carries context for step completion, including step state, output, and error information.

## StepCompletionOutcome
Enum or class representing the outcome of step completion (e.g., SUCCESS, FAILURE, RETRY, ABORT).

## Completion Processors
- **CompletionToolResultProcessor**: Handles tool result completion.
- **ExternalAbortProcessor**: Handles external abort signals.
- **ModelSignaledCompletionProcessor**: Handles model-driven completion.
- **NoValidTransitionProcossor**: Handles cases with no valid next step.
- **PlannerOverrideProcessor**: Handles planner-driven overrides.
- **RetryLimitProcessor**: Handles retry limits.
- **TimeoutProcessor**: Handles step timeouts.
- **AllCallsCompletedProcessor**: Handles completion when all calls are done.
- **GuardrailLimitProcessor**: Handles guardrail enforcement.
- **MaxModelCallLimitProcessor**: Handles model call limits.
- **MaxStepLimitProcessor**: Handles step count limits.
- **MemoryUpdateFailureProcessor**: Handles memory update failures.
- **UnrecoverableErrorProcessor**: Handles unrecoverable errors.

## Customizing Completion
- Add new processors to the StepCompletionChain.
- Implement custom logic by subclassing or composing processors.

## Extending Completion Logic
1. Implement a new completion processor.
2. Register it in the StepCompletionChain.
3. Optionally, define new StepCompletionOutcome values. 