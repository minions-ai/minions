package com.hls.minions.patient.controller;

import org.json.JSONException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@RestController
public class SessionController {

  // Replace with your standard OpenAI API key (keep this secret!)
  private final String OPENAI_API_KEY = "YOUR_STANDARD_API_KEY";

  @GetMapping("/session")
  public ResponseEntity<String> getSession() throws JSONException {
    String url = "https://api.openai.com/v1/realtime/sessions";
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + OPENAI_API_KEY);
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Request payload to mint an ephemeral token
    JSONObject body = new JSONObject();
    body.put("model", "gpt-4o-realtime-preview-2024-12-17");
    body.put("voice", "verse");

    HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    return response;
  }
}
