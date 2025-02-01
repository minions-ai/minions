package com.hls.minions.agent;

import java.util.Random;
import java.util.function.Function;

public class FraudDetectionAgent implements Function<FraudDetectionAgent.Request, FraudDetectionAgent.Response> {

    private final Random random = new Random();

    @Override
    public Response apply(Request request) {
        return new Response(Math.round(random.nextDouble() * 100 * 100.0) / 100.0);
    }


    public record Request(String claimant_name, String address) {
    }

    public record Response(Double fraudScore) {
    }
}
