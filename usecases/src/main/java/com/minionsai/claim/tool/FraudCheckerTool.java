package com.minionsai.claim.tool;

import java.util.Random;
import java.util.function.Function;

public class FraudCheckerTool implements Function<FraudCheckerTool.Request, FraudCheckerTool.Response> {

  @Override
  public Response apply(Request request) {

    return new Response(new Random().nextDouble(0.0,1.0));
  }

  public record Request(String claimId, String policyNumber, String claimantHistory) {
  }

  public record Response(Double fraudScore) {
  }
}

