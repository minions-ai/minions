package com.minionsai.core.openai;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
@Builder
public class Tool {

  @JsonProperty("type")
  @Builder.Default
  private String type = "function";

  @JsonProperty("name")
  @Builder.Default
  private String name = "get_weather";

  @JsonProperty("description")
  @Builder.Default
  private String description = "Get the current weather...";

  @JsonProperty("parameters")
  @Builder.Default
  private String parameters;
}

