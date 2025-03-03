package com.minionsai.claude.agent.factory;

import com.minionsai.claude.agent.MinionRegistry;
import com.minionsai.claude.agent.Minion;
import com.minionsai.claude.agent.memory.MemoryManager;
import com.minionsai.claude.tools.ToolRegistry;
import com.minionsai.claude.prompt.PromptService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Factory Bean responsible for instantiating agents
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MinionBeanFactory {

  private final ChatClient.Builder chatClientBuilder;
  private final MemoryManager memoryManager;
  private final ToolRegistry toolRegistry;
  private final MinionRegistry minionRegistry;
  private final PromptService promptService;

  @Autowired
  public MinionBeanFactory(ChatClient.Builder chatClientBuilder,
      MemoryManager memoryManager,
      ToolRegistry toolRegistry,
      MinionRegistry minionRegistry,
      PromptService promptService) {
    this.chatClientBuilder = chatClientBuilder;
    this.memoryManager = memoryManager;
    this.toolRegistry = toolRegistry;
    this.minionRegistry = minionRegistry;
    this.promptService = promptService;
  }

  public Minion createAgentInstance(Class<? extends Minion> agentClass, ChatMemory chatMemory) {
    try {
      return agentClass.getDeclaredConstructor(
          ChatClient.Builder.class,
          ChatMemory.class,
          MemoryManager.class,
          ToolRegistry.class,
          MinionRegistry.class,
          PromptService.class
      ).newInstance(
          chatClientBuilder,
          chatMemory,
          memoryManager,
          toolRegistry,
          minionRegistry,
          promptService
      );
    } catch (Exception e) {
      throw new RuntimeException("Failed to create agent instance", e);
    }
  }
}
