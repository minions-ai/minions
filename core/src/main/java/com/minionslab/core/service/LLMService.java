package com.minionslab.core.service;

import com.minionslab.core.service.impl.llm.model.LLMRequest;
import com.minionslab.core.service.impl.llm.model.LLMResponse;

/**
 * Interface for LLM (Large Language Model) services.
 * This interface defines the contract for processing requests using LLM models.
 */
public interface LLMService {

    /**
     * Process a request using the provided prompt.
     *
     * @param prompt  The prompt to use for processing the request
     * @param request The actual request to process
     * @return The response from the LLM
     */
    LLMResponse processRequest(LLMRequest request);
} 