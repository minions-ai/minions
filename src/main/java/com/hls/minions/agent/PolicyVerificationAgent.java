package com.hls.minions.agent;

import java.util.function.Function;

public class PolicyVerificationAgent implements Function<PolicyVerificationAgent.Request, PolicyVerificationAgent.Response> {

    public record Request(String claimant_first_name, String claimant_last_name, String claimant_phone_number,
                          String claimant_policy_number) {
    }

    public record Response(boolean policy_verified) {
    }

    public Response apply(Request request) {
        return new Response(true);
    }
}
