package com.minionsai.core.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data @Accessors(chain = true, fluent = true) @JsonIgnoreProperties(ignoreUnknown = true) public class ResponseData {

  @JsonProperty("object") private String object;

  @JsonProperty("id") private String id;

  @JsonProperty("status") private String status;

  @JsonProperty("status_details") private Object statusDetails; // Nullable

  @JsonProperty("output") private List<ResponseItem> output;

  @JsonProperty("conversation_id") private String conversationId;

  @JsonProperty("modalities") private List<String> modalities;

  @JsonProperty("voice") private String voice;

  @JsonProperty("output_audio_format") private String outputAudioFormat;

  @JsonProperty("temperature") private double temperature;

  @JsonProperty("max_output_tokens") private String maxOutputTokens;

  @JsonProperty("usage") private UsageDetails usage;

  @JsonProperty("metadatas") private Map<String, Object> metadata;
}