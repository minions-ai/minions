package com.minionslab.core.service;

import com.minionslab.core.api.dto.CreateMinionRequest;
import com.minionslab.core.api.dto.MinionRequest;
import com.minionslab.core.api.dto.MinionResponse;
import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.common.exception.MinionException.CreationException;
import com.minionslab.core.domain.MinionContext;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.Minion;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j @Service public class RequestHandlerService {


  private final PromptService promptService;
  private final MinionService minionService;
  private final ContextService contextService;

  public RequestHandlerService(MinionService minionService, PromptService promptService, ContextService contextService) {
    this.minionService = minionService;
    this.promptService = promptService;
    this.contextService = contextService;
  }

  public MinionResponse createMinion(CreateMinionRequest request) throws CreationException {
    try {

      // 2. Resolve the prompt for this request
      MinionPrompt prompt = promptService.getPrompts(request.getMinionType(), request.getPromptName(), request.getVersion())
          .orElseThrow(() -> new MinionException.PromptNotFoundException("Prompt not found: " + request.getPromptName()));

      // Create the minion without prompt
      Minion minion = minionService.createMinion(request.getMinionType(), request.getMetadata(), prompt);

      return new MinionResponse("Minion created successfully", minion.getMinionId());

    } catch (Exception e) {
      log.error("Failed to create minion", e);
      throw new MinionException.CreationException("Failed to create minion: " + e.getMessage(), e);
    }
  }

  public CompletableFuture<MinionResponse> handleRequest(String minionId, MinionRequest request) {
    try {
      Minion minion = minionService.getMinionById(minionId);
      // Context is already set up by the filter
      MinionContext context = contextService.createContext();

      return minion.processPromptAsync(request.getUserPrompt(),request.getParameters())
          .thenApply(response -> {
            // Update parameters metadata
            context.addMetadata("lastProcessedAt", System.currentTimeMillis());
            context.addMetadata("lastPromptName", request.getPromptName());

            return new MinionResponse(response, minionId);
          });

    } catch (Exception e) {
      return CompletableFuture.failedFuture(
          new MinionException.ProcessingException("Failed to process request", e)
      );
    }
  }

  public MinionResponse getMinionById(String minionId) {
    try {
      Minion minion = minionService.getMinionById(minionId);
      return new MinionResponse(null, minion.getMinionId());
    } catch (MinionException.MinionNotFoundException e) {
      log.warn("Minion not found: {}", minionId);
      throw e;
    } catch (MinionException.InvalidMinionIdException e) {
      log.error("Invalid minion ID provided: {}", minionId);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error while retrieving minion: {}", minionId, e);
      throw new MinionException("Failed to retrieve minion details", e);
    }
  }


}