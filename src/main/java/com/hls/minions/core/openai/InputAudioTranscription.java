package com.hls.minions.core.openai;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
@Builder
public class InputAudioTranscription {

  @JsonProperty("model")
  @Builder.Default
  private String model = "whisper-1";
}

