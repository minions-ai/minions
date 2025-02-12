package com.hls.minions.core.agent;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

@Slf4j
@Data
@Accessors(chain = true)
@NoArgsConstructor
public abstract class BaseAgent {

  public ChatMemory chatMemory;
  protected String prompt;
  private ChatClient chatClient;


  public BaseAgent(Builder chatClientBuilder, ChatMemory chatMemory) {
    String promptFilePath = getPromptFilePath();
    this.prompt = ResourceUtils.getText("classpath:" + promptFilePath);
    this.chatMemory = chatMemory;
    chatClient = chatClientBuilder
        .defaultFunctions(getAvailableTools())
        .defaultSystem(prompt)
        .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
        .build();
  }

  protected abstract String getPromptFilePath();

  public String processPrompt(String userRequest) {
    Message userMessage = new UserMessage(userRequest);
    String response = chatClient.prompt().messages(List.of(userMessage)).call().content();
    log.info("Agent from the LLM {}", response);
    return response;
  }


  protected abstract String[] getAvailableTools();

  protected String getSystemPrompt() {
    return this.prompt;
  }

}
