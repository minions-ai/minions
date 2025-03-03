package com.minionsai.core.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
@Builder
public class TurnDetection {

  @JsonProperty("type")
  @Builder.Default
  private String type = "server_vad";

  @JsonProperty("threshold")
  @Builder.Default
  private double threshold = 0.5;

  @JsonProperty("prefix_padding_ms")
  @Builder.Default
  private int prefixPaddingMs = 300;

  @JsonProperty("silence_duration_ms")
  @Builder.Default
  private int silenceDurationMs = 500;

  @JsonProperty("create_response")
  @Builder.Default
  private boolean createResponse = true;
}


