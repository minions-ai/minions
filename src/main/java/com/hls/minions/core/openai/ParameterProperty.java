package com.hls.minions.core.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterProperty {

  private String type;
  private String description;
  private List<String> enumValues = new ArrayList<>();

  /**
   * Deserialize a property from JSON.
   */
  public static ParameterProperty fromJson(JsonNode node) {
    ParameterProperty property = new ParameterProperty();
    property.type(node.has("type") ? node.get("type").asText() : null);
    property.description(node.has("description") ? node.get("description").asText() : null);

    // Parse enum values
    JsonNode enumNode = node.get("enum");
    if (enumNode != null && enumNode.isArray()) {
      for (JsonNode value : enumNode) {
        property.enumValues().add(value.asText());
      }
    }

    return property;
  }
}
