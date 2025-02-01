package com.hls.minions.agent;

import java.util.Random;
import java.util.function.Function;

public class DamageAssessmentAgent implements Function<DamageAssessmentAgent.Request, DamageAssessmentAgent.Response> {

    private final Random random = new Random();

    public record Request(String instructions,String claimant_first_name, String claimant_last_name, String claimant_phone_number,
                          String claimant_policy_number) {
    }

    public record Response(long estimated_damage) {
    }

    public Response apply(Request request) {

        return new Response(Math.round(random.nextDouble() * 1000));
    }
}
