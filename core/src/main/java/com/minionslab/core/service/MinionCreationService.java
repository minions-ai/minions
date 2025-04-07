package com.minionslab.core.service;

import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.enums.MinionType;
import java.util.Map;

/**
 * Service responsible for orchestrating the minion creation process.
 * This service coordinates between different components to create and initialize minions.
 */
public interface MinionCreationService {
    
    /**
     * Creates a new minion with the specified configuration.
     *
     * @param minionType The type of minion to create
     * @param metadata Custom metadata for the minion
     * @param prompt The prompt to use for the minion
     * @return The created and initialized minion
     */
    Minion createMinion(MinionType minionType, Map<String, Object> metadata, MinionPrompt prompt);
    
    /**
     * Creates a new minion with default configuration.
     *
     * @param minionType The type of minion to create
     * @return The created and initialized minion
     */
    Minion createMinion(MinionType minionType);
    
    /**
     * Creates a new minion with custom metadata.
     *
     * @param minionType The type of minion to create
     * @param metadata Custom metadata for the minion
     * @return The created and initialized minion
     */
    Minion createMinion(MinionType minionType, Map<String, Object> metadata);
} 