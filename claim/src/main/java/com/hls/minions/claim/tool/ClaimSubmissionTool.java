package com.hls.minions.claim.tool;

import com.hls.minions.claim.tool.ClaimSubmissionTool.Request;
import com.hls.minions.claim.tool.ClaimSubmissionTool.Response;
import java.util.function.Function;

public class ClaimSubmissionTool implements Function<Request, Response> {

  @Override public Response apply(Request request) {
    String validate = validate(request);
    if (validate != null && validate.length() > 0) {
      return new Response("These fields are necessary:" + validate);
    }
    return new Response("Claim info success. Claim number is 66677754");
  }

  public String validate(Request request) {
    StringBuilder missingFields = new StringBuilder();

    if (isNullOrEmpty(request.claimant_first_name)) {
      missingFields.append("claimant_first_name, ");
    }
    if (isNullOrEmpty(request.claimant_last_name)) {
      missingFields.append("claimant_last_name, ");
    }
    if (isNullOrEmpty(request.claimant_email)) {
      missingFields.append("claimant_email, ");
    }
    if (isNullOrEmpty(request.claimant_phone)) {
      missingFields.append("claimant_phone, ");
    }
    if (isNullOrEmpty(request.accident_location)) {
      missingFields.append("accident_location, ");
    }
    if (isNullOrEmpty(request.accident_latitude)) {
      missingFields.append("accident_latitude, ");
    }
    if (isNullOrEmpty(request.accident_longitude)) {
      missingFields.append("accident_longitude, ");
    }
    if (isNullOrEmpty(request.policy_number)) {
      missingFields.append("policy_number, ");
    }
    if (isNullOrEmpty(request.fraud_score)) {
      missingFields.append("fraud_score, ");
    }

    return missingFields.toString();

  }

  private boolean isNullOrEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  public record Request(
      String claimant_first_name,
      String claimant_last_name,
      String claimant_email,
      String claimant_phone,
      String accident_location,
      String accident_latitude,
      String accident_longitude,
      String policy_number,
      String fraud_score) {

  }

  public record Response(String message) {

  }
}
