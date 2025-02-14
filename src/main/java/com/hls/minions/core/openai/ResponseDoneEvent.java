package com.hls.minions.core.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseDoneEvent {

  @JsonProperty("type")
  private String type;

  @JsonProperty("event_id")
  private String eventId;

  @JsonProperty("response")
  private ResponseData response;

  public static ResponseDoneEvent fromJson(String json) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(json, ResponseDoneEvent.class);
  }
}











