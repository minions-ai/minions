package com.minionsai.core.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ConversationItem {

  @JsonProperty("type")
  private String type;

  @JsonProperty("call_id")
  private String callId;

  @JsonProperty("output")
  private String output;
}
