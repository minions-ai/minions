package com.hls.minions.core.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Event {

  public String toJson() {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "{}"; // Return an empty JSON object in case of error
    }
  }
}
