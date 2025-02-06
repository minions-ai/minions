package com.hls.minions.claim.tool;

import java.util.function.Function;

public class TowDispatchTool implements Function<TowDispatchTool.Request, TowDispatchTool.Response> {

  @Override
  public Response apply(Request request) {
    return new Response("Tow truck dispatched", "15 minutes");
  }

  public record Request(String location, String vehicleType) {
  }

  public record Response(String status, String estimatedArrivalTime) {
  }
}
