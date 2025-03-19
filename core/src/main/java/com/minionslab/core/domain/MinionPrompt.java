package com.minionslab.core.domain;

import com.minionslab.core.domain.enums.MinionType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Builder.Default;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a system prompt with its content and metadata. This class is designed to be stored in a document database (MongoDB).
 */

@Data
@Document(collection = "prompts")
@SuperBuilder
public class MinionPrompt extends BaseEntity {

  @Default
  private final List<String> contents = new ArrayList<>();
  @Default
  private final Map<String, Object> metadata = new HashMap<>();
  @Id
  private String id;
  @NotNull
  private String name;
  @NotNull
  private MinionType type;
  @NotNull
  private String version;

  public void addContent(String content) {
    contents.add(content);
  }
}