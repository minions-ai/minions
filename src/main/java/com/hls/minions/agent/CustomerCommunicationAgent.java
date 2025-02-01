package com.hls.minions.agent;

import com.sun.net.httpserver.Request;

import java.util.function.Function;

public class CustomerCommunicationAgent implements Function<CustomerCommunicationAgent.Request, CustomerCommunicationAgent.Response> {


    @Override
    public Response apply(Request request) {
        return new Response("Communication with customer was done. Customer is happy.");
    }

    public record Request(String initialUserRequest, String masterAgentCommandPrompt) {
    }

    public record Response(String response) {
    }
}
