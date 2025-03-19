package com.minionslab.core.domain.enums;

public enum PromptType {
  SYSTEM,              // Core system instructions that define the agent's capabilities and constraints
  PERSONA,             // Character definition and personality traits
  CONTEXT,             // Background information and current situation
  EXAMPLES,            // Few-shot examples to guide the model's behavior
  TASK_SPECIFIC,       // Instructions specific to a particular task
  GUIDELINES,          // Operational rules and guidelines
  CONSTRAINTS,         // Limitations and restrictions on behavior
  MEMORY,              // Recalled information from past interactions
  REASONING_FRAMEWORK, // Frameworks for structured reasoning or decision-making
  TOOL_DESCRIPTIONS,   // Descriptions of available tools and how to use them
  ERROR_HANDLING,      // Instructions for dealing with errors or edge cases
  FORMAT_INSTRUCTIONS, // Output format requirements
  METADATA,            // Information about the prompt itself
  USER_PREFERENCES,    // Stored user preferences for personalization
  DOMAIN_KNOWLEDGE,    // Specialized knowledge for specific domains
  POLICY,              // Organizational policies or compliance requirements
  REFLECTION,          // Instructions for self-assessment or improvement
  DYNAMIC,
  REQUEST_TEMPLATE,     // Template for formatting user requests to the agent
  USER_TEMPLATE;
}