package com.hls.minions.core.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenDetails {

  @JsonProperty("text_tokens")
  private int textTokens;

  @JsonProperty("audio_tokens")
  private int audioTokens;

  @JsonProperty("cached_tokens")
  private int cachedTokens;

  @JsonProperty("cached_tokens_details")
  private CachedTokenDetails cachedTokensDetails;
}
