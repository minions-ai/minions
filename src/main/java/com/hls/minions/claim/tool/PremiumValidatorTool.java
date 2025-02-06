package com.hls.minions.claim.tool;

import java.util.function.Function;

public class PremiumValidatorTool implements Function<PremiumValidatorTool.Request, PremiumValidatorTool.Response> {

  @Override
  public Response apply(Request request) {
    return new Response(true, "Premium paid until July 2025");
  }

  public record Request(String policyNumber) {
  }

  public record Response(boolean isPaid, String paymentStatus) {
  }
}

