package com.hls.minions.core.service;

import com.hls.minions.core.agent.MasterAgentPrompt;
import com.hls.minions.core.openai.AudioBufferAppend;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Slf4j @Service
public class OpenAIWebSocketService {

  public static final String MODEL_NAME = "gpt-4o-realtime-preview-2024-12-17";
  private static final String OPENAI_WS_URL = "wss://api.openai.com/v1/realtime?model=" + MODEL_NAME;
  private static final String WEBSOCKET_HEADERS = "OpenAI-Beta: realtime=v1";
  @Value("spring.ai.openai.api-key")
  private static String OPENAI_API_KEY = "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"; // Replace with your key
  // Map client session IDs to persistent OpenAI WebSocket connections.
  final ConcurrentHashMap<String, WebSocket> openAIConnections = new ConcurrentHashMap<>();
  private final OkHttpClient client = new OkHttpClient();
  private final FunctionCallService functionCallService;
  private final MasterAgentPrompt masterAgentPrompt;

  private Map<String, OpenAiWebSocketListener> listenerMap = new ConcurrentHashMap<>();

  public OpenAIWebSocketService(FunctionCallService functionCallService, MasterAgentPrompt masterAgentPrompt) {
    this.functionCallService = functionCallService;
    this.masterAgentPrompt = masterAgentPrompt;
  }
  // Map client session IDs to conversation IDs for continued conversations.


  /**
   * Initializes a persistent WebSocket connection to OpenAI for the given client session. If a connection already exists, it reuses it.
   */
  public void initializeConnection(WebSocketSession clientSession) {

    String clientId = clientSession.getId();
    // If already initialized, do nothing.
    if (openAIConnections.containsKey(clientId)) {
      System.out.println("Persistent connection already exists for session: " + clientId);
      return;
    }

    Request request = new Request.Builder()
        .url(OPENAI_WS_URL)
        .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
        .addHeader("OpenAI-Beta", "realtime=v1")
        .build();

    OpenAiWebSocketListener listener = listenerMap.computeIfAbsent(clientId,
        id -> new OpenAiWebSocketListener(functionCallService, clientSession, masterAgentPrompt));

    WebSocket webSocket = client.newWebSocket(request, listener);
    // Cache the persistent connection.
    openAIConnections.put(clientId, webSocket);
  }


  /**
   * Constructs the JSON payload for continuing a conversation with VAD enabled.
   */
  private String getResponseCreatePayload(String conversationId) throws JSONException {
    JSONObject payload = new JSONObject();
    payload.put("type", "response.create");
    payload.put("conversation_id", conversationId);
    payload.put("modality", new JSONArray().put("audio"));

    JSONObject serverVad = new JSONObject();
    serverVad.put("silence_duration_ms", 500);
    payload.put("server_vad", serverVad);

    JSONObject system = new JSONObject();
    system.put("prompt", "Continue helping the user based on previous context.");
    JSONArray tools = new JSONArray();
  /*  JSONObject weatherTool = new JSONObject();
    weatherTool.put("name", "weather");
    weatherTool.put("description", "Get the current weather for a location.");
    JSONObject parameters = new JSONObject();
    parameters.put("location", "string");
    weatherTool.put("parameters", parameters);
    tools.put(weatherTool);
    system.put("tools", tools);*/
    payload.put("system", system);

    return payload.toString();
  }

  // Optionally, you can implement methods to send audio chunks or end signals using the cached connection.
  public void sendAudioChunk(WebSocketSession clientSession, byte[] chunk) {
    String clientId = clientSession.getId();
    WebSocket openAISocket = openAIConnections.get(clientId);
    if (openAISocket != null) {
      try {
        /*        byte[] bytes = AudioConverter.convertWebMtoPCM(chunk);*/
        AudioBufferAppend audioEvent = AudioBufferAppend.builder().audio(Base64.getEncoder().encodeToString(chunk)).build();
        openAISocket.send(audioEvent.toJson());
      } catch (Exception e) {
//        log.error("Error converting audio chunk to openAI connection: " + e.getMessage());
      }
    } else {
      System.err.println("No persistent OpenAI connection found for session: " + clientId);
    }
  }

  public void sendEndSignal(WebSocketSession clientSession) {
    String clientId = clientSession.getId();
    WebSocket openAISocket = openAIConnections.get(clientId);
    if (openAISocket != null) {
      // Optionally, you could send a specific payload for turn finalization if needed.
      // For now, we do nothing as VAD handles turn detection.
      System.out.println("End signal not explicitly sent; relying on VAD for turn detection.");
    } else {
      System.err.println("No persistent OpenAI connection found for session: " + clientId);
    }
  }

  public void closeConnection(WebSocketSession clientSession) {
    String clientId = clientSession.getId();
    WebSocket openAISocket = openAIConnections.remove(clientId);
    if (openAISocket != null) {
      openAISocket.close(1000, "Client session closed");
      System.out.println("Closed persistent connection for session: " + clientId);
    }
  }

}
