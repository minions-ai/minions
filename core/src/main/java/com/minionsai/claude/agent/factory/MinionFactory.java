package com.minionsai.claude.agent.factory;


import static com.minionsai.claude.agent.factory.MinionType.AUTOMATION_ENGINEER;
import static com.minionsai.claude.agent.factory.MinionType.COMMUNICATION_AGENT;
import static com.minionsai.claude.agent.factory.MinionType.INTEGRATION_AGENT;
import static com.minionsai.claude.agent.factory.MinionType.LOGGING_AGENT;
import static com.minionsai.claude.agent.factory.MinionType.RESOURCE_MANAGER;
import static com.minionsai.claude.agent.factory.MinionType.SECURITY_MONITOR;
import static com.minionsai.claude.agent.factory.MinionType.TESTING_AGENT;
import static com.minionsai.claude.agent.factory.MinionType.USER_SUPPORT;
import static com.minionsai.claude.agent.factory.MinionType.WORKFLOW_MANAGER;

import com.minionsai.claude.agent.Minion;
import com.minionsai.claude.agent.MinionRegistry;
import com.minionsai.claude.agent.memory.MemoryManager;
import com.minionsai.claude.prompt.PromptService;
import com.minionsai.claude.prompt.SystemPrompt;
import com.minionsai.claude.tools.ToolRegistry;
import com.minionsai.claude.workflow.WorkflowManagerMinion;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Factory Bean for creating and instantiating agent instances
 */
@Service
@Slf4j
public class MinionFactory {

  private final Map<MinionType, Class<? extends Minion>> agentTypeRegistry = new HashMap<>();
  private final ChatClient.Builder chatClientBuilder;
  private final ToolRegistry toolRegistry;
  private final MinionRegistry minionRegistry;
  private final PromptService promptService;
  private final MemoryManager memoryManager;
  private final MinionBeanFactory minionBeanFactory;

  @Autowired
  public MinionFactory(ChatClient.Builder chatClientBuilder,
      ToolRegistry toolRegistry,
      MinionRegistry minionRegistry,
      PromptService promptService,
      MemoryManager memoryManager,
      MinionBeanFactory minionBeanFactory) {
    this.chatClientBuilder = chatClientBuilder;
    this.toolRegistry = toolRegistry;
    this.minionRegistry = minionRegistry;
    this.promptService = promptService;
    this.memoryManager = memoryManager;
    this.minionBeanFactory = minionBeanFactory;
  }

  @PostConstruct
  public void initialize() {
    agentTypeRegistry.computeIfAbsent(WORKFLOW_MANAGER,minionType -> new WorkflowManagerMinion(chatClientBuilder, chatMemory, memoryManager, toolRegistry, minionRegistry));
    switch (type) {
      case WORKFLOW_MANAGER:
        return new WorkflowManagerMinion(chatClientBuilder, chatMemory, memoryManager, toolRegistry, minionRegistry);
           case COMMUNICATION_AGENT:
        return new CommunicationAgentMinion(chatClientBuilder, chatMemory, memoryManager, toolRegistry, minionRegistry);
      case SECURITY_MONITOR:
        return new SecurityMonitorMinion(chatClientBuilder, chatMemory, memoryManager, toolRegistry, minionRegistry);
      case RESOURCE_MANAGER:
        return new ResourceManagerMinion(chatClientBuilder, chatMemory, memoryManager, toolRegistry, minionRegistry);
      case USER_SUPPORT:
        return new UserSupportMinion(chatClientBuilder, chatMemory, memoryManager, toolRegistry, minionRegistry);
      case AUTOMATION_ENGINEER:
        return new AutomationEngineerMinion(chatClientBuilder, chatMemory, memoryManager, toolRegistry, minionRegistry);
      case INTEGRATION_AGENT:
        return new IntegrationAgentMinion(chatClientBuilder, chatMemory, memoryManager, toolRegistry, minionRegistry);
      case TESTING_AGENT:
        return new TestingAgentMinion(chatClientBuilder, chatMemory, memoryManager, toolRegistry, minionRegistry);
      case LOGGING_AGENT:
        return new LoggingAgentMinion(chatClientBuilder, chatMemory, memoryManager, toolRegistry, minionRegistry);
      default:
        throw new IllegalArgumentException("Unknown MinionType: " + type);
    }
  }

  public void registerAgentType(MinionType typeName, Class<? extends Minion> agentClass) {
    agentTypeRegistry.put(typeName, agentClass);
    log.info("Registered agent type: {}", typeName);
  }

  public Minion createAgent(MinionType agentType, String minionName) {
    SystemPrompt prompt = promptService.getLatestPromptForAgentType(agentType)
        .orElseThrow(() -> new IllegalArgumentException("No prompt found for agent type: " + agentType));
    return createAgent(agentType, minionName, prompt);
  }

  public Minion createAgent(MinionType agentType, String minionName, SystemPrompt prompt) {
    Class<? extends Minion> agentClass = agentTypeRegistry.get(agentType);
    if (agentClass == null) {
      throw new IllegalArgumentException("Unknown agent type: " + agentType);
    }

    ChatMemory chatMemory = new InMemoryChatMemory();
    Minion agent = minionBeanFactory.createAgentInstance(agentClass, chatMemory);

    agent.setName(prompt.getName());
    agent.setDescription(prompt.getDescription());
    agent.updateSystemPrompt(prompt);

    log.info("Created agent of type: {}", agentType);
    return agent;
  }


}

