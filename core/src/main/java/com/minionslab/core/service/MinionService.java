package com.minionslab.core.service;


import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.Map;

/**
 * Service interface for managing minions and their interactions.
 */
@Validated
public interface MinionService {

  /**
   * Creates a new minion.
   *
   * @param minionType The type of minion to create
   * @param metadata The metadata for the minion
   * @param promptEntityId The ID of the prompt to use
   * @param effectiveDate The date when this minion should become effective
   * @param expiryDate The date when this minion should expire
   * @return The created minion
   */
  Minion createMinion(
      @NotNull MinionType minionType,
      Map<String, Object> metadata,
      @NotBlank String promptEntityId,
      Instant effectiveDate,
      Instant expiryDate);

  /**
   * Processes a request using the specified minion.
   *
   * @param minionId The ID of the minion to use
   * @param request  The request to process
   * @param context additional context for processing the request
   * @return The processed response
   */
  String processRequest(@NotBlank String minionId, @NotBlank String request, Map<String, Object> context);

  /**
   * Retrieves a minion by its ID.
   *
   * @param minionId The ID of the minion to retrieve
   * @return The minion if found
   */
  Minion getMinion(@NotBlank String minionId);
}

