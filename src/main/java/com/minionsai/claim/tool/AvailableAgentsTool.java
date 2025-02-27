package com.minionsai.claim.tool;


import com.minionsai.claim.tool.AvailableAgentsTool.Request;
import com.minionsai.claim.tool.AvailableAgentsTool.Response;
import java.util.List;
import java.util.function.Function;

public class AvailableAgentsTool implements Function<Request, Response> {

  @Override
  public Response apply(AvailableAgentsTool.Request request) {
    List<Agent> responses = List.of(
        new Agent("ClaimCreationAgent", "Handles the creation of claims, ensuring all necessary information is captured and structured correctly."),
        new Agent("ClaimIntakeAgent", "Processes incoming claim requests, validating initial details and routing them appropriately."),
        new Agent("FraudInvestigationAgent", "Analyzes claims for potential fraud using AI models, behavioral analytics, and historical claim patterns."),
        new Agent("PolicyVerificationAgent", "Verifies policyholder information, checks policy status, and ensures coverage details match the claim."),
        new Agent("TowDispatchAgent", "Coordinates the dispatch of towing services based on location, service availability, and claim priority.")
    );

    return new Response(responses);
  }

  public record Request(String description) {}

  public record Response(List<Agent> response) {}

  public record Agent(String agentName, String agentResponsibility) {}
}