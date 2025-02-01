package com.hls.minions.agent;

import java.util.Random;
import java.util.function.Function;

public class ClaimSubmissionAgent implements Function<ClaimSubmissionAgent.Request, ClaimSubmissionAgent.Response> {

  private Random random = new Random();

  @Override public Response apply(Request request) {
    return new Response(String.valueOf(Math.round(random.nextDouble() * 10000)));
  }

  public static record Request(String detailed_instructions,Claim claim){}

  public static record Response(String claimNumber) {

  }

  public record Claim(String claimant_first_name, String claimant_last_name, String claimant_email, String claimant_phone_number,
                      String policy_number, String loss_location, String data_of_loss) {

  }

}
