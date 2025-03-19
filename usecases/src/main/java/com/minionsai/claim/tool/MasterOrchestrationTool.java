package com.minionsai.claim.tool;

import com.minionslab.core.domain.Minion;
import com.minionsai.claim.service.ClaimAgentManager;
import java.util.function.Function;

public class MasterOrchestrationTool implements Function<MasterOrchestrationTool.Request, MasterOrchestrationTool.Response> {

  private final ClaimAgentManager claimAgentManager;

  public MasterOrchestrationTool(ClaimAgentManager claimAgentManager) {
    this.claimAgentManager = claimAgentManager;
  }

  @Override
  public Response apply(Request request) {
    if(request.requestId == null) {
      return new Response("failed","RequestId cannot be null");
    }
    if(request.nextAgentName == null) {
      return new Response("failed","nextAgentName cannot be null. Please provide the exact agent name without any space in the name");
    }
    Minion nextAgent = claimAgentManager.getOrCreateAgent(request.requestId, request.nextAgentName);
    nextAgent.processPrompt(request.taskDetails);
    return new Response("Agent dispatched successfully", "Next agent: " + request.nextAgentName);
  }

  public record Request(String requestId, String currentAgentName, String nextAgentName, String taskDetails) {
  }

  public record Response(String status, String nextStep) {
  }
}
