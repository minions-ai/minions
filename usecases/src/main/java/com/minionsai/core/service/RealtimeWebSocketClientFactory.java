package com.minionsai.core.service;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Service
public class RealtimeWebSocketClientFactory {

  private static final Logger logger = LoggerFactory.getLogger(RealtimeWebSocketClientFactory.class);
  private static final String API_TOKEN = "your_openai_api_token"; // Replace with your API token
  private static final String MODEL = "gpt-4o-realtime-preview-2024-12-17"; // Choose an OpenAI model
  private static final String WEBSOCKET_URL = "wss://api.openai.com/v1/realtime?model=" + MODEL;
  private static final String WEBSOCKET_HEADERS = "OpenAI-Beta: realtime=v1";


  private final ConcurrentHashMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<String, WebSocketSession>();
  private final AtomicInteger sessionCounter = new AtomicInteger(0);

  public WebSocketSession createWebSocketSession(String requestId) {
    try {
      StandardWebSocketClient client = new StandardWebSocketClient();
      WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
      headers.add(WebSocketHttpHeaders.AUTHORIZATION,"Bearer " + API_TOKEN);
      headers.add("",WEBSOCKET_HEADERS);
      WebSocketHandler handler = new RealtimeWebSocketHandler(requestId);
      Future<WebSocketSession> futureSession = client.doHandshake(handler, WEBSOCKET_URL);
      WebSocketSession session = futureSession.get();

      logger.info("WebSocket session {} created.", requestId);
      return session;
    } catch (Exception e) {
      logger.error("Failed to create WebSocket session", e);
      return null;
    }
  }

  public void sendAudio(String sessionId, ByteBuffer audioData) {
    WebSocketSession session = activeSessions.computeIfAbsent(sessionId, key -> {
      return createWebSocketSession(sessionId);
    });

    if (session != null && session.isOpen()) {
      try {

        session.sendMessage(new BinaryMessage(audioData));
        logger.info("Sent audio data to session {}.", sessionId);
      } catch (Exception e) {
        logger.error("Failed to send audio data to session {}", sessionId, e);
      }
    } else {
      logger.warn("WebSocket session {} is not open.", sessionId);
    }
  }

  public void closeSession(int sessionId) {
    WebSocketSession session = activeSessions.remove(sessionId);
    if (session != null) {
      try {
        session.close();
        logger.info("Closed WebSocket session {}", sessionId);
      } catch (Exception e) {
        logger.error("Failed to close WebSocket session {}", sessionId, e);
      }
    }
  }

  private class RealtimeWebSocketHandler extends AbstractWebSocketHandler {

    private final String sessionId;

    public RealtimeWebSocketHandler(String requestId) {
      this.sessionId = requestId;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
      logger.info("WebSocket session {} established.", sessionId);
      sendAuthentication(session);
    }

    private void sendAuthentication(WebSocketSession session) {
      try {
        JSONObject authMessage = new JSONObject();
        authMessage.put("type", "authorization");
        authMessage.put("authorization", "Bearer " + API_TOKEN);
        session.sendMessage(new TextMessage(authMessage.toString()));
        logger.info("Session {} authenticated.", sessionId);
      } catch (Exception e) {
        logger.error("Failed to send authentication for session {}", sessionId, e);
      }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
      logger.info("Session {} received text message: {}", sessionId, message.getPayload());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
      logger.info("Session {} received binary message (size: {})", sessionId, message.getPayloadLength());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
      logger.error("Session {} encountered an error", sessionId, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
      logger.info("Session {} closed: {}", sessionId, status);
      activeSessions.remove(sessionId);
    }
  }
}
