package com.minionslab.core.service.impl;

import com.minionslab.core.common.exception.MinionException.MinionCreationException;
import com.minionslab.core.domain.Minion;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.MinionRecipe;
import com.minionslab.core.domain.MinionRecipeRegistry;
import com.minionslab.core.domain.MinionRegistry;
import com.minionslab.core.domain.enums.MinionType;
import com.minionslab.core.domain.tools.ToolRegistry;
import com.minionslab.core.event.MinionEventPublisher;
import com.minionslab.core.service.MinionCreationService;
import com.minionslab.core.service.MinionLifecycleManager;
import com.minionslab.core.service.impl.llm.LLMServiceFactory;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MinionCreationServiceImpl implements MinionCreationService {

    private final MinionRegistry minionRegistry;
    private final MinionRecipeRegistry recipeRegistry;
    private final MinionLifecycleManager lifecycleManager;
    private final ToolRegistry toolRegistry;
    private final LLMServiceFactory llmServiceFactory;
    private final MinionEventPublisher eventPublisher;
    private final MinionFactory minionFactory;
    
    public MinionCreationServiceImpl(
            MinionRegistry minionRegistry,
            MinionRecipeRegistry recipeRegistry,
            MinionLifecycleManager lifecycleManager,
            ToolRegistry toolRegistry,
            LLMServiceFactory llmServiceFactory,
            MinionEventPublisher eventPublisher,
            MinionFactory minionFactory) {
        this.minionRegistry = minionRegistry;
        this.recipeRegistry = recipeRegistry;
        this.lifecycleManager = lifecycleManager;
        this.toolRegistry = toolRegistry;
        this.llmServiceFactory = llmServiceFactory;
        this.eventPublisher = eventPublisher;
        this.minionFactory = minionFactory;
    }

    @Override
    public Minion createMinion(MinionType minionType, Map<String, Object> metadata, MinionPrompt prompt) {
        log.debug("Creating minion of type: {} with prompt: {}", minionType, prompt != null ? prompt.getId() : "null");
        
        try {
            // Get and validate recipe
            MinionRecipe recipe = recipeRegistry.getRecipe(minionType);
            if (prompt != null) {
                recipe.validatePrompt(prompt);
            }
            
            // Merge metadata with defaults
            Map<String, Object> finalMetadata = new HashMap<>(recipe.getDefaultMetadata());
            if (metadata != null) {
                finalMetadata.putAll(metadata);
            }
            
            // Create minion instance
            Minion minion = minionFactory.createMinion(minionType, finalMetadata, prompt);
            
            // Initialize minion
            lifecycleManager.initializeMinion(minion);
            
            // Register minion
            minionRegistry.registerMinion(minion);
            
            log.info("Created minion of type: {} with ID: {}", minionType, minion.getMinionId());
            return minion;
            
        } catch (Exception e) {
            log.error("Failed to create minion of type: {}", minionType, e);
            throw new MinionCreationException("Failed to create minion: " + e.getMessage(), e);
        }
    }

    @Override
    public Minion createMinion(MinionType minionType) {
        return createMinion(minionType, null, null);
    }

    @Override
    public Minion createMinion(MinionType minionType, Map<String, Object> metadata) {
        return createMinion(minionType, metadata, null);
    }
} 