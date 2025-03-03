package com.minionsai.claim.tool;

import java.util.function.Function;

public class PolicyDatabaseTool implements Function<PolicyDatabaseTool.Request, PolicyDatabaseTool.Response> {

  @Override
  public Response apply(Request request) {
    return new Response("Policy Found", "Active", "Comprehensive Coverage");
  }

  public record Request(String policyNumber) {
  }

  public record Response(String status, String policyType, String coverageDetails) {
  }
}
