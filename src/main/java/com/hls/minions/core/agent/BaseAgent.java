package com.hls.minions.core.agent;

import com.hls.minions.core.annotation.AgentPrompt;
import com.hls.minions.core.service.prompt.AgentPromptLoader;
import com.hls.minions.core.view.Modality;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatModel;

@Slf4j
@Data
@Accessors(chain = true)
public abstract class BaseAgent {

  public ChatMemory chatMemory;
  protected String prompt;
  protected ChatModel chatModel;
  private ChatClient chatClient;
  private AgentPromptLoader loader;

  public BaseAgent(AgentPromptLoader loader, Builder builder, ChatMemory chatMemory) {
    this(builder, chatMemory, null);
    this.loader = loader;
  }


  public BaseAgent(Builder chatClientBuilder, ChatMemory chatMemory, Modality modality) {
    this.prompt = ResourceUtils.getText("classpath:" + resolvePromptFilePath());
    this.chatMemory = chatMemory;
    prompt = loader.loadPrompt(this.getClass(), null, null);
  }

  private String resolvePromptFilePath() {
    AgentPrompt annotation = this.getClass().getAnnotation(AgentPrompt.class);
    if (annotation == null) {
      throw new IllegalStateException("Missing @AgentPrompt annotation on " + this.getClass().getName());
    }
    return annotation.value();
  }


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
