package com.minionslab.core.service.impl.llm;

import java.util.List;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

public class MinionHybridMemory implements ChatMemory {

  @Override public void add(String conversationId, Message message) {
    ChatMemory.super.add(conversationId, message);
  }

  @Override public void add(String conversationId, List<Message> messages) {

  }

  @Override public List<Message> get(String conversationId, int lastN) {
    return List.of();
  }

  @Override public void clear(String conversationId) {

  }
}
