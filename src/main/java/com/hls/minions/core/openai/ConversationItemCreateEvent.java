package com.hls.minions.core.openai;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ConversationItemCreateEvent extends Event {


  @JsonProperty("type")
  private String type;
  @JsonProperty("item")
  private ConversationItem item;

  public static ConversationItemCreateEvent fromJson(String json) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(json, ConversationItemCreateEvent.class);
  }
}

