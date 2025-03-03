package com.minionsai.claim.tool;

import java.util.function.Function;

public class HistoricalClaimsTool implements Function<HistoricalClaimsTool.Request, HistoricalClaimsTool.Response> {

  @Override
  public Response apply(Request request) {
    return new Response(2, "Previous claims: 2021 Theft, 2023 Accident");
  }

  public record Request(String claimantId) {
  }

  public record Response(int previousClaimsCount, String claimsSummary) {
  }
}
