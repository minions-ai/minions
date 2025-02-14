package com.hls.minions.core.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsageDetails {

  @JsonProperty("total_tokens")
  private int totalTokens;

  @JsonProperty("input_tokens")
  private int inputTokens;

  @JsonProperty("output_tokens")
  private int outputTokens;

  @JsonProperty("input_token_details")
  private TokenDetails inputTokenDetails;

  @JsonProperty("output_token_details")
  private TokenDetails outputTokenDetails;
}
