package com.hls.minions.core.service;

public record Response(
    String requestId,
    String response,
    String audioBase64) {

}
