package com.minionsai.core.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.Builder;

import java.util.Arrays;
import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
@Builder
public class Session {

  @JsonProperty("modalities")
  @Builder.Default
  private List<String> modalities = Arrays.asList("text", "audio");

  @JsonProperty("instructions")
  @Builder.Default
  private String instructions = "You are a helpful assistant.";

  @JsonProperty("voice")
  @Builder.Default
  private String voice = "sage";

  @JsonProperty("input_audio_format")
  @Builder.Default
  private String inputAudioFormat = "pcm16";

  @JsonProperty("output_audio_format")
  @Builder.Default
  private String outputAudioFormat = "pcm16";

  @JsonProperty("input_audio_transcription")
  @Builder.Default
  private InputAudioTranscription inputAudioTranscription = InputAudioTranscription.builder().build();

  @JsonProperty("turn_detection")
  @Builder.Default
  private TurnDetection turnDetection = TurnDetection.builder().build();

  @JsonProperty("tools")
  @Builder.Default
  private List<Tool> tools = Arrays.asList(Tool.builder().build());

  @JsonProperty("tool_choice")
  @Builder.Default
  private String toolChoice = "auto";

  @JsonProperty("temperature")
  @Builder.Default
  private double temperature = 0.8;

  @JsonProperty("max_response_output_tokens")
  @Builder.Default
  private String maxResponseOutputTokens = "inf";
}
