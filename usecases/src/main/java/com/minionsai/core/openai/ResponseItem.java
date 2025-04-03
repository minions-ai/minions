package com.minionsai.core.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data @Accessors(chain = true, fluent = true) @JsonIgnoreProperties(ignoreUnknown = true) public class ResponseItem {

  @JsonProperty("id") private String id;

  @JsonProperty("object") private String object;

  @JsonProperty("type") private String type;

  @JsonProperty("status") private String status;

  @JsonProperty("role") private String role;

  @JsonProperty("name") private String name;

  @JsonProperty("call_id") private String callId;

  @JsonProperty("arguments")
  private String arguments; // Stored as raw JSON string

  @JsonProperty("text")
  private List<ResponseContent> content;
}