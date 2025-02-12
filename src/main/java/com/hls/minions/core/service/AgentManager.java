package com.hls.minions.core.service;

import com.hls.minions.core.agent.BaseAgent;
import com.hls.minions.core.agent.BaseAudioAgent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.stereotype.Service;

@Slf4j @Service public abstract class AgentManager {

  protected final ChatClient.Builder chatClientBuilder;
  protected final Map<String, BaseAgent> masterAgentMap = new ConcurrentHashMap<>();
  protected final Map<String, ChatMemory> chatMemoryMap = new ConcurrentHashMap<>();
  protected final Map<String, Map<String, BaseAgent>> requestAgentsMap = new ConcurrentHashMap<>();

  public AgentManager(ChatClient.Builder chatClientBuilder) {
    this.chatClientBuilder = chatClientBuilder;
  }

  /**
   * Generates a unique request ID (temporary claim number).
   */
  public String generateRequestId() {
    return "REQ-" + UUID.randomUUID().toString().substring(0, 8);
  }


  /**
   * Handles execution of the request asynchronously.
   */
  protected String execute(String requestId, String requestText, BaseAgent masterAgent) {
    AtomicReference<String> responseText = new AtomicReference<>();
    responseText.set(masterAgent.processPrompt("RequestId:" + requestId + "\nRequestText:" + requestText));

    return responseText.get();
  }

  protected byte[] execute(String requestId, Object requestData, BaseAudioAgent masterAgent) {
    return masterAgent.processPrompt(requestId, requestData);
  }

  /**
   * Creates a new Master Agent and its associated Chat Memory.
   */
  protected abstract BaseAgent createMasterAgent(String requestId, ChatMemory chatMemory);


  /**
   * Executes a prompt using the Master Agent.
   */
  public String executePrompt(String requestId, String requestText) {
    ChatMemory chatMemory = new InMemoryChatMemory();
    chatMemoryMap.put(requestId, chatMemory);
    BaseAgent masterAgent = masterAgentMap.computeIfAbsent(requestId, id -> createMasterAgent(id, chatMemory));
    return execute(requestId, requestText, masterAgent);
  }

  public byte[] executePrompt(String requestId, Object requestData) {
    ChatMemory chatMemory = new InMemoryChatMemory();
    chatMemoryMap.put(requestId, chatMemory);
    BaseAgent masterAgent = masterAgentMap.computeIfAbsent(requestId, id -> createMasterAgent(id, chatMemory));
    return execute(requestId, requestData, (BaseAudioAgent) masterAgent);
  }

  /**
   * Retrieves or creates an agent for the given request ID and agent type.
   */
  public <T extends BaseAgent> T getOrCreateAgent(String requestId, String agentName) {
    requestAgentsMap.putIfAbsent(requestId, new ConcurrentHashMap<>());
    Map<String, BaseAgent> agentMap = requestAgentsMap.get(requestId);

    return (T) agentMap.computeIfAbsent(agentName, agentKey -> createAgent(requestId, agentName));
  }

  /**
   * Dynamically loads and instantiates an agent class.
   */
  private BaseAgent createAgent(String requestId, String agentName) {
    ChatMemory chatMemory = chatMemoryMap.get(requestId);
    if (chatMemory == null) {
      throw new IllegalStateException("ChatMemory not found for requestId: " + requestId);
    }

    String fullClassName = "com.hls.minions.claim.agent." + agentName;
    try {
      // Load class dynamically
      Class<?> agentClass = Class.forName(fullClassName, true, Thread.currentThread().getContextClassLoader());

      // Validate class type
      if (!BaseAgent.class.isAssignableFrom(agentClass)) {
        throw new IllegalArgumentException("Class " + agentName + " is not a valid BaseAgent subclass.");
      }

      // Instantiate the agent
      return (BaseAgent) agentClass.asSubclass(BaseAgent.class).getConstructor(ChatClient.Builder.class, ChatMemory.class)
          .newInstance(chatClientBuilder, chatMemory);

    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Agent class not found: " + fullClassName, e);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to create agent: " + fullClassName + " for requestId: " + requestId, e);
    }
  }

  /**
   * Cleans up agents and memory after request processing is complete.
   */
  public void cleanupRequest(String requestId) {
    masterAgentMap.remove(requestId);
    chatMemoryMap.remove(requestId);
    requestAgentsMap.remove(requestId);
    log.info("Cleaned up resources for Request ID: {}", requestId);
  }


}
