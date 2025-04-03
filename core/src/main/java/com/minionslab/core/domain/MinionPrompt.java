package com.minionslab.core.domain;

import com.minionslab.core.common.exception.PromptException;
import com.minionslab.core.domain.enums.PromptType;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a system prompt with its text and metadatas. This class is designed to be stored in a document database (MongoDB). Each prompt
 * is immutable once created and versioned. Updates create new versions with effective dates. The active version at any point in time is
 * determined by the effective and expiry dates.
 */
@Data
@Document(collection = "minion_prompts")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@CompoundIndexes({
    @CompoundIndex(name = "prompt_primary_key", def = "{'entityId': 1, 'version': 1}", unique = true)
})
public class MinionPrompt extends EffectiveDatedEntity {

  @Builder.Default
  private Map<PromptType, PromptComponent> components = new HashMap<>();

  @Builder.Default
  private Map<String, Object> metadata = new HashMap<>();

  @NotNull
  private String description;

  @NotNull
  @Default
  private String version = "1";

  @Builder.Default
  private boolean deployed = false;

  @Builder.Default
  private Set<String> toolboxes = new HashSet<>();

  /**
   * Sets the entityId. This method ensures that the entityId cannot be changed once it's set.
   *
   * @throws PromptException if attempting to change an existing entityId
   */
  public void setEntityId(String entityId) {
    if (this.entityId != null && !this.entityId.equals(entityId)) {
      throw new PromptException("Cannot change entityId once it's set");
    }
    this.entityId = entityId;
  }

  /**
   * Creates a new version of this prompt with updated components. The current version will be automatically expired at the effective date
   * of the new version.
   *
   * @param component     The new component to add
   * @param newVersion    The version number for the new version
   * @param effectiveDate The date when this version becomes effective
   * @return A new MinionPrompt instance with the updated component
   * @throws PromptException if the version already exists
   */
  public MinionPrompt createNewVersion(Instant effectiveDate) {
    String newVersion = incrementVersion();
    MinionPrompt newPrompt = MinionPrompt.builder()
        .entityId(entityId)  // Keep the same entityId
        .description(description)
        .version(newVersion)
        .components(components)
        .metadata(metadata)
        .effectiveDate(effectiveDate)
        .build();
    this.setExpiryDate(effectiveDate);
    return newPrompt;
  }

  private String incrementVersion() {
    String[] parts = version.split("\\.");
    int lastPart = Integer.parseInt(parts[parts.length - 1]);
    parts[parts.length - 1] = String.valueOf(lastPart + 1);
    return String.join(".", parts);
  }

  /**
   * Checks if this prompt is active at the given point in time
   */
  public boolean isActiveAt(Instant pointInTime) {
    return (expiryDate == null || expiryDate.isAfter(pointInTime)) &&
        effectiveDate.isBefore(pointInTime);
  }

  /**
   * Checks if this prompt is currently active
   */
  public boolean isActive() {
    return isActiveAt(Instant.now());
  }

  public boolean isLocked() {
    return isActive() && deployed;
  }
}