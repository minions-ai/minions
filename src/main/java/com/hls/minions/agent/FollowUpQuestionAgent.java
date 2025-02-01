package com.hls.minions.agent;

import java.util.function.Function;

public class FollowUpQuestionAgent implements Function<FollowUpQuestionAgent.Request, FollowUpQuestionAgent.Response> {

    @Override
    public Response apply(Request request) {
        return new Response("Response");
    }

    public record Request(String phone_number, String txt_message) {
    }

    public record Response(String sms_transcription) {
    }
}
