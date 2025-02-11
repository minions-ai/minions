package com.hls.minions.claim.tool;

import com.hls.minions.claim.service.ClaimService;
import java.util.function.Function;


public class CustomerCommunicationTool implements Function<CustomerCommunicationTool.Request, CustomerCommunicationTool.Response> {


  private final ClaimService claimService;

  public CustomerCommunicationTool(ClaimService claimService) {
    this.claimService = claimService;
  }

  @Override
  public Response apply(Request request) {
    claimService.completeClaim(request.requestId, request.messageContent);
    return new Response(request.requestId, "Message Sent Successfully");
  }

  public record Request(String requestId, String messageContent, String channel) {

  }

  public record Response(String requestId, String communicationText) {

  }
}
