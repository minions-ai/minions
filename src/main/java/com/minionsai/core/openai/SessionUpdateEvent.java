package com.minionsai.core.openai;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true, fluent = true)
@Builder
public class SessionUpdateEvent extends Event {

  @JsonProperty("session")
  @Builder.Default
  private Session session = Session.builder().build();
  @JsonProperty("event_id")
  @Builder.Default
  private String eventId = "event_123";
  @JsonProperty("type")
  @Builder.Default
  private String type = "session.update";

}


