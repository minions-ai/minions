package com.minionsai.claim.tool;

import java.util.function.Function;

public class LoggingTool implements Function<LoggingTool.Request, LoggingTool.Response> {

  @Override
  public Response apply(Request request) {
    return new Response("Log Entry Created Successfully");
  }

  public record Request(String agentName, String action, String details, String timestamp) {
  }

  public record Response(String logStatus) {
  }
}
