package com.hls.minions.core.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.util.*;

@Data
@Accessors(chain = true, fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolParameters {

  private String type;
  private Map<String, Map<String, Object>> properties = new HashMap<>();
  private List<String> required = new ArrayList<>();

  /**
   * Deserialize JSON into a `ToolParameters` object.
   */
  public static ToolParameters fromJson(String json) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode rootNode = objectMapper.readTree(json);

    ToolParameters schema = new ToolParameters();
    schema.type(rootNode.has("type") ? rootNode.get("type").asText() : null);

    // Parse properties correctly
    JsonNode propertiesNode = rootNode.get("properties");
    if (propertiesNode != null && propertiesNode.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        schema.properties().put(field.getKey(), objectMapper.convertValue(field.getValue(), Map.class));
      }
    }

    // Parse required fields
    JsonNode requiredNode = rootNode.get("required");
    if (requiredNode != null && requiredNode.isArray()) {
      for (JsonNode field : requiredNode) {
        schema.required().add(field.asText());
      }
    }

    return schema;
  }

  /**
   * Serialize the `ToolParameters` object into the required JSON format.
   */
  public String toJson() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode rootNode = objectMapper.createObjectNode();

    rootNode.put("type", this.type);

    // Convert properties to proper JSON structure
    ObjectNode propertiesNode = objectMapper.createObjectNode();
    for (Map.Entry<String, Map<String, Object>> entry : properties.entrySet()) {
      propertiesNode.set(entry.getKey(), objectMapper.valueToTree(entry.getValue()));
    }
    rootNode.set("properties", propertiesNode);

    // Convert required list to JSON array
    if (!required.isEmpty()) {
      rootNode.set("required", objectMapper.valueToTree(required));
    }

    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
  }
}
