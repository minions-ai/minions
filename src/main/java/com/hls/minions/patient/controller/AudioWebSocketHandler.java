package com.hls.minions.patient.controller;

import com.hls.minions.patient.service.PatientAudioAgentManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@Component
public class AudioWebSocketHandler extends BinaryWebSocketHandler {

  private final PatientAudioAgentManager audioAgentManager;

  // Map to buffer incoming audio data for each session.
  private final ConcurrentHashMap<String, ByteArrayOutputStream> sessionBuffers = new ConcurrentHashMap<>();

  public AudioWebSocketHandler(PatientAudioAgentManager audioAgentManager) {
    this.audioAgentManager = audioAgentManager;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    // Increase allowed buffer sizes as needed.
    session.setBinaryMessageSizeLimit(512 * 1024); // e.g. 512 KB
    session.setTextMessageSizeLimit(512 * 1024);
    System.out.println("WebSocket connection established with session: " + session.getId());
    // Initialize a buffer for this session.
    sessionBuffers.put(session.getId(), new ByteArrayOutputStream());
  }

  @Override
  protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
    System.out.println("Received binary message from session: " + session.getId());
    // Copy the payload into a byte array.
    ByteBuffer payload = message.getPayload();
    byte[] bytes = new byte[payload.remaining()];
    payload.get(bytes);

    // Buffer the received chunk.
    ByteArrayOutputStream buffer = sessionBuffers.get(session.getId());
    if (buffer != null) {
      buffer.write(bytes);
    } else {
      System.err.println("No buffer found for session: " + session.getId());
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    // We assume the client sends "end" to signal the final chunk.
    String payload = message.getPayload().trim();
    System.out.println("Received text message from session: " + session.getId() + " payload: " + payload);
    if ("end".equalsIgnoreCase(payload)) {
      // Retrieve and remove the buffered data.
      ByteArrayOutputStream buffer = sessionBuffers.remove(session.getId());
      if (buffer != null) {
        byte[] completeAudioData = buffer.toByteArray();
        // Process the complete data.
        byte[] responseBytes = audioAgentManager.executePrompt(session.getId(), completeAudioData);
        // Send back the processed response.
        try {
          session.sendMessage(new BinaryMessage(responseBytes));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        System.err.println("No audio data found for session: " + session.getId());
      }
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
    System.out.println("WebSocket connection closed for session: " + session.getId() + " with status: " + status);
    // Clean up the buffer for this session.
    sessionBuffers.remove(session.getId());
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    System.err.println("Transport error on session " + session.getId() + ": " + exception.getMessage());
  }
}
