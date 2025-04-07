package com.minionslab.core.service.impl.llm;

import com.minionslab.core.domain.ChateMemoryStrategy;
import java.util.List;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import reactor.core.publisher.Flux;

public class MinionsMemoryAdvisor extends AbstractChatMemoryAdvisor {


  protected MinionsMemoryAdvisor(ChatMemory chatMemory) {
    super(chatMemory);
  }

  protected MinionsMemoryAdvisor(ChatMemory chatMemory, String defaultConversationId, int defaultChatMemoryRetrieveSize,
      boolean protectFromBlocking) {
    super(chatMemory, defaultConversationId, defaultChatMemoryRetrieveSize, protectFromBlocking);
  }

  protected MinionsMemoryAdvisor(ChatMemory chatMemory, String defaultConversationId, int defaultChatMemoryRetrieveSize,
      boolean protectFromBlocking, int order) {
    super(chatMemory, defaultConversationId, defaultChatMemoryRetrieveSize, protectFromBlocking, order);
  }

  @Override public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
    return null;
  }

  @Override public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
    return null;
  }



}
