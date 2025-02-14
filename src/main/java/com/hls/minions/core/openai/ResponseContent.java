package com.hls.minions.core.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseContent {

  @JsonProperty("type")
  private String type;

  @JsonProperty("transcript")
  private String transcript;
}