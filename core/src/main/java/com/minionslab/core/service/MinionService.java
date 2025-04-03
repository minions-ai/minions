package com.minionslab.core.service;

import com.minionslab.core.api.dto.CreateMinionRequest;
import com.minionslab.core.domain.Minion;

/**
 * Service interface for managing minions and their interactions.
 */
public interface MinionService {

    /**
     * Creates a new minion.
     *
     * @param minion The minion to create
     * @return The created minion
     */
    Minion createMinion(CreateMinionRequest minion);

    /**
     * Processes a request using the specified minion.
     *
     * @param minionId The ID of the minion to use
     * @param request The request to process
     * @return The response from the LLM service
     */
    String processRequest(String minionId, String request);

    /**
     * Retrieves a minion by its ID.
     *
     * @param minionId The ID of the minion to retrieve
     * @return The minion if found
     */
    Minion getMinion(String minionId);
}

