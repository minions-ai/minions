package com.minionsai.claim.service;

import com.minionsai.claim.agent.MasterAgent;
import com.minionsai.core.service.AgentManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClaimAgentManager extends AgentManager {


  public ClaimAgentManager(ChatClient.Builder chatClientBuilder) {
    super(chatClientBuilder);
  }


  @Override protected MasterAgent createMasterAgent(String requestId, ChatMemory chatMemory) {
    return new MasterAgent(chatClientBuilder, chatMemory);
  }
}
