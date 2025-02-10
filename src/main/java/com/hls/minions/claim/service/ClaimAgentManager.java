package com.hls.minions.claim.service;

import com.hls.minions.claim.agent.MasterAgent;
import com.hls.minions.core.service.AgentManager;
import com.hls.minions.core.view.Modality;
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


  @Override protected MasterAgent createMasterAgent(String requestId, ChatMemory chatMemory, Modality modality) {
    return new MasterAgent(chatClientBuilder, chatMemory);
  }
}
