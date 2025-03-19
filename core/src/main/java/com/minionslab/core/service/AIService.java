package com.minionslab.core.service;

import org.springframework.ai.chat.client.ChatClient;

public interface AIService {

  ChatClient getChatClient();
}
