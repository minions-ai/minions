package com.hls.minions.agent;

import com.sun.net.httpserver.Request;

import java.util.function.Function;

public class CustomerCommunicationAgent implements Function<Request, CustomerCommunicationAgent.Response> {
    @Override
    public Response apply(com.sun.net.httpserver.Request request) {
        return null;
    }

    public record Request(String initialUserRequest, String masterAgentCommandPrompt) {
    }

    public record Response(String response) {
    }
}
