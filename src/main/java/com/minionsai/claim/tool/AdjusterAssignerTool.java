package com.minionsai.claim.tool;


import java.util.function.Function;

public class AdjusterAssignerTool implements Function<AdjusterAssignerTool.Request, AdjusterAssignerTool.Response> {

  @Override
  public Response apply(Request request) {
    return new Response("Adjuster Assigned", "Adjuster Name: " + request.adjusterName);
  }

  public record Request(String claimId, String severityLevel, String adjusterName) {
  }

  public record Response(String status, String assignedAdjuster) {
  }
}
