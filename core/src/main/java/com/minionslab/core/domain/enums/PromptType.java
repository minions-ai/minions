package com.minionslab.core.domain.enums;

public enum PromptType {
  SYSTEM(
      "Core system instructions that define the agent's capabilities, constraints, and fundamental behavior patterns. These instructions form the foundation of how the agent operates and interacts.",
      "Should be defined first and remain relatively stable.",
      "All other types, as it sets the base behavior.",
      "Keep it focused on core capabilities, avoid task-specific details.",
      "Making it too specific or including implementation details."
  ),

  PERSONA(
      "Character definition and personality traits that shape how the agent presents itself and interacts with users. This includes tone, style, and behavioral characteristics.",
      "Define after SYSTEM, can be customized per user or context.",
      "CONTEXT, USER_PREFERENCES, and FORMAT_INSTRUCTIONS.",
      "Maintain consistency, align with user expectations.",
      "Over-personalization or conflicting traits."
  ),

  CONTEXT(
      "Background information and current situation details that provide the agent with necessary context for understanding and responding to user requests appropriately.",
      "Update dynamically based on conversation flow and user state.",
      "MEMORY, DOMAIN_KNOWLEDGE, and USER_PREFERENCES.",
      "Keep it relevant and concise, update regularly.",
      "Including outdated or irrelevant information."
  ),

  EXAMPLES(
      "Few-shot examples that demonstrate desired behavior patterns and response formats. These help guide the model's understanding of expected outputs and interaction styles.",
      "Provide after core instructions, update based on performance.",
      "FORMAT_INSTRUCTIONS and TASK_SPECIFIC.",
      "Use diverse, representative examples.",
      "Too few examples or non-representative cases."
  ),

  TASK_SPECIFIC(
      "Instructions specific to a particular task or domain, providing detailed guidance on how to handle specialized requests and maintain domain-specific knowledge.",
      "Define when handling specialized tasks, can be combined with DOMAIN_KNOWLEDGE.",
      "DOMAIN_KNOWLEDGE, GUIDELINES, and TOOL_DESCRIPTIONS.",
      "Keep it focused and well-structured.",
      "Overlapping with SYSTEM or being too generic."
  ),

  GUIDELINES(
      "Operational rules and guidelines that define how the agent should approach problems, make decisions, and handle various situations while maintaining consistency.",
      "Define after SYSTEM, update based on performance.",
      "CONSTRAINTS, POLICY, and REASONING_FRAMEWORK.",
      "Make rules clear and actionable.",
      "Conflicting or overly complex rules."
  ),

  CONSTRAINTS(
      "Limitations and restrictions on behavior, defining what the agent should not do and setting boundaries for its actions and responses.",
      "Define alongside SYSTEM, update as needed.",
      "POLICY, GUIDELINES, and ERROR_HANDLING.",
      "Be specific and unambiguous.",
      "Too restrictive or vague constraints."
  ),

  MEMORY(
      "Recalled information from past interactions and stored knowledge that helps maintain context and continuity across conversations.",
      "Update continuously, maintain relevance.",
      "CONTEXT, USER_PREFERENCES, and DOMAIN_KNOWLEDGE.",
      "Implement efficient storage and retrieval.",
      "Information overload or outdated data."
  ),

  REASONING_FRAMEWORK(
      "Structured frameworks for decision-making and problem-solving, providing systematic approaches to analyze situations and generate appropriate responses.",
      "Define after SYSTEM, can be task-specific.",
      "GUIDELINES, DOMAIN_KNOWLEDGE, and TOOL_DESCRIPTIONS.",
      "Keep frameworks simple and adaptable.",
      "Over-complication or rigid frameworks."
  ),

  TOOL_DESCRIPTIONS(
      "Detailed descriptions of available tools, their capabilities, usage patterns, and integration points within the agent's workflow.",
      "Update when tools change, maintain accuracy.",
      "TASK_SPECIFIC, REASONING_FRAMEWORK, and ERROR_HANDLING.",
      "Include clear examples and error cases.",
      "Outdated or incomplete documentation."
  ),

  ERROR_HANDLING(
      "Instructions for detecting, handling, and recovering from errors or edge cases, ensuring graceful degradation and appropriate error responses.",
      "Define comprehensive error strategies, update based on incidents.",
      "CONSTRAINTS, POLICY, and TOOL_DESCRIPTIONS.",
      "Cover all common error scenarios.",
      "Incomplete error coverage or unclear recovery steps."
  ),

  FORMAT_INSTRUCTIONS(
      "Specific requirements for output formatting, including structure, style, and presentation guidelines for different types of responses.",
      "Define based on user needs and response types.",
      "PERSONA, USER_PREFERENCES, and EXAMPLES.",
      "Be consistent and user-friendly.",
      "Overly complex or inconsistent formatting."
  ),

  METADATA(
      "Information about the prompt itself, including version, author, creation date, and other administrative details that help track and manage prompts.",
      "Maintain for all prompt types, update regularly.",
      "All other types for version tracking.",
      "Keep it complete and up-to-date.",
      "Missing or outdated metadata."
  ),

  USER_PREFERENCES(
      "Stored user preferences and customization settings that allow the agent to adapt its behavior and responses to individual user needs.",
      "Update based on user interactions and feedback.",
      "PERSONA, CONTEXT, and FORMAT_INSTRUCTIONS.",
      "Respect privacy and maintain consistency.",
      "Over-personalization or conflicting preferences."
  ),

  DOMAIN_KNOWLEDGE(
      "Specialized knowledge and expertise for specific domains, ensuring accurate and contextually appropriate responses within particular fields.",
      "Define for each relevant domain, keep current.",
      "TASK_SPECIFIC, REASONING_FRAMEWORK, and CONTEXT.",
      "Maintain accuracy and relevance.",
      "Outdated or incorrect domain information."
  ),

  POLICY(
      "Organizational policies, compliance requirements, and regulatory guidelines that ensure the agent's behavior aligns with required standards.",
      "Define and enforce consistently, update as needed.",
      "CONSTRAINTS, GUIDELINES, and ERROR_HANDLING.",
      "Keep policies clear and enforceable.",
      "Conflicting or unclear policies."
  ),

  REFLECTION(
      "Instructions for self-assessment and improvement, enabling the agent to evaluate its performance and adjust its behavior accordingly.",
      "Implement feedback loops, update based on performance.",
      "MEMORY, USER_PREFERENCES, and ERROR_HANDLING.",
      "Regular assessment and adjustment.",
      "Insufficient feedback or delayed updates."
  ),

  DYNAMIC(
      "Real-time, context-dependent instructions that adapt based on current conditions, user state, or environmental factors.",
      "Update based on current state and requirements.",
      "All other types, especially CONTEXT.",
      "Maintain consistency while adapting.",
      "Too frequent changes or inconsistent adaptation."
  ),

  REQUEST_TEMPLATE(
      "Template for formatting and structuring user requests to ensure consistent and clear communication with the agent.",
      "Define based on expected request types and formats.",
      "FORMAT_INSTRUCTIONS and USER_TEMPLATE.",
      "Keep templates flexible and clear.",
      "Too rigid or complex templates."
  ),

  USER_TEMPLATE(
      "Template for user-specific interaction patterns and preferences, defining how individual users prefer to communicate with the agent.",
      "Customize per user, update based on preferences.",
      "PERSONA, USER_PREFERENCES, and REQUEST_TEMPLATE.",
      "Balance personalization with consistency.",
      "Over-customization or conflicting templates."
  );

  private final String description;
  private final String usage;
  private final String interactsWith;
  private final String bestPractices;
  private final String pitfalls;

  PromptType(String description, String usage, String interactsWith, String bestPractices, String pitfalls) {
    this.description = description;
    this.usage = usage;
    this.interactsWith = interactsWith;
    this.bestPractices = bestPractices;
    this.pitfalls = pitfalls;
  }

  public String getDescription() {
    return description;
  }

  public String getUsage() {
    return usage;
  }

  public String getInteractsWith() {
    return interactsWith;
  }

  public String getBestPractices() {
    return bestPractices;
  }

  public String getPitfalls() {
    return pitfalls;
  }

  @Override
  public String toString() {
    return String.format("""
            %s Prompt Type:
            Description: %s
            
            Usage Guidelines:
            - %s
            
            Interactions:
            - Works with: %s
            
            Best Practices:
            - %s
            
            Common Pitfalls to Avoid:
            - %s
            """,
        name(),
        description,
        usage,
        interactsWith,
        bestPractices,
        pitfalls
    );
  }
}