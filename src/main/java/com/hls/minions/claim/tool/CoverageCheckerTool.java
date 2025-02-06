package com.hls.minions.claim.tool;

import java.util.function.Function;

public class CoverageCheckerTool implements Function<CoverageCheckerTool.Request, CoverageCheckerTool.Response> {

  @Override
  public Response apply(Request request) {
    if(request.policyNumber == null) {
      return new Response(false,"Policy number is needed.");
    }
    if(request.incidentType == null) {
      return new Response(false,"Incident type is needed.");
    }
    return new Response(true, "Collision Damage is covered.");
  }

  public record Request(String policyNumber, String incidentType) {
  }

  public record Response(boolean isCovered, String explanation) {
  }
}
