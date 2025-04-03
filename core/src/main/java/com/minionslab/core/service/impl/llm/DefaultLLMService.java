package com.minionslab.core.service.impl.llm;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.exception.LLMServiceException;
import com.minionslab.core.service.LLMService;
import com.minionslab.core.service.impl.llm.model.LLMRequest;
import com.minionslab.core.service.impl.llm.model.LLMResponse;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * Default implementation of LLMService that uses a simple template-based approach. This is a placeholder implementation that can be
 * replaced with actual LLM provider integration.
 */
@Slf4j
@Service
public class DefaultLLMService implements LLMService {

  @Override
  public LLMResponse processRequest(LLMRequest request) {
    String requestId = UUID.randomUUID().toString();

    // Validate inputs
    validateInputs(request);

    MinionPrompt prompt = request.getPrompt();

    try {
      // Get and validate components
      Map<PromptType, PromptComponent> components = prompt.getComponents();
      PromptComponent systemComponent = components.get(PromptType.SYSTEM);
      PromptComponent userComponent = components.get(PromptType.USER_TEMPLATE);

      String userPrompt = request.getUserRequest();
      if (userComponent != null) {
        // Replace the request placeholder in the user component
        userPrompt = userComponent.getText().replace("{request}", request.getUserRequest());
      }

      // For now, just return a mock response
      // TODO: Replace with actual LLM provider integration
      String responseText = getLLMResponse(systemComponent, userPrompt);

      log.debug("Request [{}] processed successfully", requestId);

      return LLMResponse.builder()
          .requestId(requestId)
          .promptId(prompt.getId())
          .promptVersion(prompt.getVersion())
          .responseText(responseText)
          .timestamp(Instant.now())
          .build();

    } catch (Exception e) {
      log.error("Error processing request [{}]: {}", requestId, e.getMessage(), e);
      throw new LLMServiceException("Failed to process request: " + e.getMessage(), e);
    }
  }

  private static @NotNull String getLLMResponse(PromptComponent systemComponent, String userPrompt) {
    return String.format("System: %s\nUser: %s\nResponse: This is a mock response from the LLM service.",
        systemComponent.getText(),
        userPrompt);
  }

  private void validateInputs(LLMRequest request) {

  }
} 