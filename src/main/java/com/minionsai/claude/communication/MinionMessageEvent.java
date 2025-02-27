package com.minionsai.claude.communication;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Event for asynchronous message passing between Minions
 */
@Data
@AllArgsConstructor
public class MinionMessageEvent {
  private final MinionMessage message;
}
