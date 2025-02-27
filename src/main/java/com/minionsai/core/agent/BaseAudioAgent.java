package com.minionsai.core.agent;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.AudioParameters;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.AudioParameters.AudioResponseFormat;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.AudioParameters.Voice;
import org.springframework.util.MimeType;

@Slf4j
@Data
@Accessors(chain = true)
public abstract class BaseAudioAgent extends BaseAgent {


  public ChatMemory chatMemory;
  protected String prompt;


  public BaseAudioAgent(Builder chatClientBuilder, ChatMemory chatMemory) {
    super(chatClientBuilder, chatMemory);
  }

  protected abstract String getPromptFilePath();

  public byte[] processPrompt(String requestId, Object userRequest) {

    Media media = Media.builder().mimeType(MimeType.valueOf("audio/mp3")).data(userRequest).build();
    OpenAiChatOptions options = OpenAiChatOptions.builder()
        .model("gpt-4o-audio-preview")
        .outputModalities(List.of("text", "audio"))
        .outputAudio(new AudioParameters(Voice.NOVA, AudioResponseFormat.MP3))
        .build();
    ChatResponse response = getChatClient().prompt().user(u -> u.text("Respond to the voice with a voice").media(media))
        .options(options).call().chatResponse();
    byte[] dataAsByteArray = response.getResult().getOutput().getMedia().get(0).getDataAsByteArray();
    log.info("Agent from the LLM {}", response);
    return dataAsByteArray;
  }


  protected abstract String[] getAvailableTools();

  protected String getSystemPrompt() {
    return this.prompt;
  }

}
