package com.hls.minions.core.service;


import java.nio.ByteBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@Component
public class AudioWebSocketHandler extends BinaryWebSocketHandler {

  private final OpenAIWebSocketService openAIWebSocketService;

  public AudioWebSocketHandler(OpenAIWebSocketService openAIWebSocketService) {
    this.openAIWebSocketService = openAIWebSocketService;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    // Increase allowed message buffer sizes as needed.
    session.setBinaryMessageSizeLimit(512 * 1024); // 512 KB
    session.setTextMessageSizeLimit(512 * 1024);
    System.out.println("WebSocket connection established with session: " + session.getId());
    // Initialize a persistent connection to OpenAI for this client session.
    openAIWebSocketService.initializeConnection(session);
  }

  @Override
  protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
//    System.out.println("Forwarding binary message from session: " + session.getId());
    // Get the binary payload.
    ByteBuffer payload = message.getPayload();
    byte[] bytes = new byte[payload.remaining()];
    payload.get(bytes);
    // Immediately forward the audio chunk to OpenAI.
    openAIWebSocketService.sendAudioChunk(session, bytes);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message)  {
    String payload = message.getPayload().trim();
    System.out.println("Received text message from session: " + session.getId() + " payload: " + payload);
    // When the client sends "end", forward this signal to OpenAI.
    if ("end".equalsIgnoreCase(payload)) {
      openAIWebSocketService.sendEndSignal(session);
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status)  {
    System.out.println("WebSocket connection closed for session: " + session.getId() + " with status: " + status);
    openAIWebSocketService.closeConnection(session);
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    System.err.println("Transport error on session " + session.getId() + ": " + exception.getMessage());
  }
}
