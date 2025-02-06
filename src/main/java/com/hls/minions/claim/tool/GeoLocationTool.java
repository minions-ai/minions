package com.hls.minions.claim.tool;

import java.util.Random;
import java.util.function.Function;

public class GeoLocationTool implements Function<GeoLocationTool.Request, GeoLocationTool.Response> {

  private static final Random RANDOM = new Random();

  @Override
  public Response apply(Request request) {
    double randomLatitude = getRandomLatitude();
    double randomLongitude = getRandomLongitude();

    String locationAnalysis = String.format("Generated random location: (%.6f, %.6f)", randomLatitude, randomLongitude);
    return new Response(true, locationAnalysis, randomLatitude, randomLongitude);
  }

  private double getRandomLatitude() {
    return -90 + (180 * RANDOM.nextDouble()); // Range: -90 to 90
  }

  private double getRandomLongitude() {
    return -180 + (360 * RANDOM.nextDouble()); // Range: -180 to 180
  }

  public record Request(String claimId, String reportedLocation) {}

  public record Response(boolean locationValid, String locationAnalysis, double latitude, double longitude) {}
}
