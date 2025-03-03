package com.minionsai.core.openai;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CachedTokenDetails {

  @JsonProperty("text_tokens")
  private int textTokens;

  @JsonProperty("audio_tokens")
  private int audioTokens;
}