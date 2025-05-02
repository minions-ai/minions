package com.minionsai.core.service;

import com.minionslab.core.domain.Minion;
import com.minionsai.core.agent.BaseAudioAgent;
import com.minionslab.core.service.ChatMemoryFactory;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Slf4j @Service public abstract class AgentManager {

  protected final ChatClient.Builder chatClientBuilder;
  protected final Map<String, Minion> masterAgentMap = new ConcurrentHashMap<>();
  protected final Map<String, ChatMemory> chatMemoryMap = new ConcurrentHashMap<>();
  protected final Map<String, Map<String, Minion>> requestAgentsMap = new ConcurrentHashMap<>();
  protected final ChatMemoryFactory chatMemoryFactory;

  public AgentManager(ChatClient.Builder chatClientBuilder, ChatMemoryFactory chatMemoryFactory) {
    this.chatClientBuilder = chatClientBuilder;
    this.chatMemoryFactory = chatMemoryFactory;
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
  protected String execute(String requestId, String requestText, Minion masterAgent) {
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
  protected abstract Minion createMasterAgent(String requestId, ChatMemory chatMemory);


  /**
   * Executes a messages using the Master Agent.
   */
  public String executePrompt(String requestId, String requestText) {
    ChatMemory chatMemory = chatMemoryFactory.createDefaultChatMemory();
    chatMemoryMap.put(requestId, chatMemory);
    Minion masterAgent = masterAgentMap.computeIfAbsent(requestId, id -> createMasterAgent(id, chatMemory));
    return execute(requestId, requestText, masterAgent);
  }

  public byte[] executePrompt(String requestId, Object requestData) {
    ChatMemory chatMemory = chatMemoryFactory.createDefaultChatMemory();
    chatMemoryMap.put(requestId, chatMemory);
    Minion masterAgent = masterAgentMap.computeIfAbsent(requestId, id -> createMasterAgent(id, chatMemory));
    return execute(requestId, requestData, (BaseAudioAgent) masterAgent);
  }

  /**
   * Retrieves or creates an agent for the given request ID and agent minionType.
   */
  public <T extends Minion> T getOrCreateAgent(String requestId, String agentName) {
    requestAgentsMap.putIfAbsent(requestId, new ConcurrentHashMap<>());
    Map<String, Minion> agentMap = requestAgentsMap.get(requestId);

    return (T) agentMap.computeIfAbsent(agentName, agentKey -> createAgent(requestId, agentName));
  }

  /**
   * Dynamically loads and instantiates an agent class.
   */
  private Minion createAgent(String requestId, String agentName) {
    ChatMemory chatMemory = chatMemoryMap.get(requestId);
    if (chatMemory == null) {
      throw new IllegalStateException("ChatMemory not found for requestId: " + requestId);
    }

    String fullClassName = "com.hls.minions.claim.agent." + agentName;
    try {
      // Load class dynamically
      Class<?> agentClass = Class.forName(fullClassName, true, Thread.currentThread().getContextClassLoader());

      // Validate class minionType
      if (!Minion.class.isAssignableFrom(agentClass)) {
        throw new IllegalArgumentException("Class " + agentName + " is not a valid Minion subclass.");
      }

      // Instantiate the agent
      return (Minion) agentClass.asSubclass(Minion.class).getConstructor(ChatClient.Builder.class, ChatMemory.class)
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
