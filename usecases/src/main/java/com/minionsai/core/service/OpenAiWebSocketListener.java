package com.minionsai.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minionsai.core.agent.MasterAgentPrompt;
import com.minionsai.core.openai.ConversationItem;
import com.minionsai.core.openai.ConversationItemCreateEvent;
import com.minionsai.core.openai.ResponseContent;
import com.minionsai.core.openai.ResponseData;
import com.minionsai.core.openai.ResponseDoneEvent;
import com.minionsai.core.openai.ResponseItem;
import com.minionsai.core.openai.Session;
import com.minionsai.core.openai.SessionUpdateEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.json.JSONObject;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class OpenAiWebSocketListener extends WebSocketListener {

  private final ConcurrentHashMap<String, String> conversationIds = new ConcurrentHashMap<>();
  //  private final OpenAIWebSocketService openAIWebSocketService;
  private final String clientId;
  private final WebSocketSession clientSession;
  private final ByteArrayOutputStream audioBuffer = new ByteArrayOutputStream();
  private final ByteArrayOutputStream functionParameter = new ByteArrayOutputStream();
//  @Value("classpath:agents/patient/master_agent.txt")
//  private String SYSTEM_PROMPT;
  private final MasterAgentPrompt masterAgentPrompt;
  private FunctionCallService functionCallService;


  public OpenAiWebSocketListener(FunctionCallService functionCallService,
      WebSocketSession clientSession, MasterAgentPrompt masterAgentPrompt) {
    this.functionCallService = functionCallService;

    this.clientId = clientSession.getId();
    this.clientSession = clientSession;

    this.masterAgentPrompt = masterAgentPrompt;
  }

  private static String getClientTranscript(String transcript, String person) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> messageMap = new HashMap<>();
    messageMap.put("person", person);
    messageMap.put("transcript", transcript);

    String message = objectMapper.writeValueAsString(messageMap);
    return message;
  }

  private String getSessionUpdatePayload() {
    SessionUpdateEvent sessionUpdateEvent = SessionUpdateEvent.builder().build();
    Session session = sessionUpdateEvent.session();
//    session.voice("alloy");
    session.instructions(getAgentSystemPrompt());
    session.tools(functionCallService.getTools());
    return sessionUpdateEvent.toJson();
  }

  @Override
  public void onOpen(WebSocket webSocket, Response response) {
    System.out.println("✅ Persistent OpenAI connection opened for session: " + clientId);

    // Send session.create payload to initialize a new conversation.
    String payload = getSessionUpdatePayload();
    System.out.println("Sending session payload: " + payload);
    webSocket.send(payload);

  }

  @Override
  public void onMessage(WebSocket webSocket, String text) {
//    System.out.println("🔹 Received text message on persistent connection for session " + clientId + ": " + text);
    try {
      JSONObject json = new JSONObject(text);

      // Update conversation ID if provided
      if (json.has("conversation_id")) {
        String convId = json.getString("conversation_id");
        conversationIds.put(clientId, convId);
        System.out.println("Updated conversation_id for session " + clientId + ": " + convId);
      }

      // Handle audio chunks (response.audio.delta)
      if ("response.audio.delta".equals(json.optString("type"))) {
        if (json.has("delta")) {
          String base64Audio = json.getString("delta");
          byte[] audioBytes = Base64.getDecoder().decode(base64Audio);
          audioBuffer.write(audioBytes);
//          System.out.println("🔄 Buffered audio chunk (size: " + audioBytes.length + " bytes).");
        } else {
//          System.err.println("⚠️ Received response.audio.delta event but no delta field found.");
        }
      }
      // When response is done, forward the full audio buffer to the client
      else if ("response.done".equals(json.optString("type"))) {
        log.info("Response done for session {}. \n Data:{}", clientId, text);
        ResponseDoneEvent responseDoneEvent = ResponseDoneEvent.fromJson(text);

        ResponseData response = responseDoneEvent.response();
        if (response != null) {
          List<ResponseItem> output = response.output();
          for (ResponseItem responseItem : output) {
            String type = responseItem.type();
            if ("function_call".equals(type)) {
              String arguments = responseItem.arguments();
              String callId = responseItem.callId();
              String call = functionCallService.call(responseItem.name(), arguments);
              ConversationItemCreateEvent conversationItemCreateEvent = new ConversationItemCreateEvent();
              ConversationItem conversationItem = new ConversationItem();
              conversationItemCreateEvent.item(conversationItem);
              conversationItemCreateEvent.type("conversation.item.create");
              conversationItem.callId(callId);
              conversationItem.type("function_call_output");
              conversationItem.output(call);
              String functionCallResult = conversationItemCreateEvent.toJson();
              webSocket.send(functionCallResult);
            } else if ("message".equals(type)) {
              List<ResponseContent> content = responseItem.content();
              if (content.size() > 0) {
                ResponseContent responseContent = content.get(0);
                String transcript = responseContent.transcript();
                String message = getClientTranscript(transcript, "Agent");
                clientSession.sendMessage(new TextMessage(message));
              }

              System.out.println("✅ Response complete. Sending buffered audio to client.");

              byte[] fullAudioData = audioBuffer.toByteArray();
              if (fullAudioData.length > 0) {
                clientSession.sendMessage(new BinaryMessage(fullAudioData));
                System.out.println("📤 Sent full audio data (size: " + fullAudioData.length + " bytes) to client.");
              } else {
                System.err.println("⚠️ Received response.done but audio buffer is empty.");
              }

              // Clear the buffer after sending
              audioBuffer.reset();
            }

          }
        }

      } else if ("conversation.item.input_audio_transcription.completed".equals(json.optString("type"))) {
        String transcript = json.getString("transcript");
        String message = getClientTranscript(transcript, "Caller");
        clientSession.sendMessage(new TextMessage(message));
      }
      // Forward other messages (text transcriptions, etc.)
      else {
//        clientSession.sendMessage(new TextMessage(text));
      }

    } catch (Exception e) {
      System.err.println("❌ Error processing text message on persistent connection: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void onMessage(WebSocket webSocket, ByteString bytes) {
    System.out.println("🔊 Received binary message on persistent connection for session " + clientId);
    try {
      // Forward AI-generated audio to the client.
      clientSession.sendMessage(new BinaryMessage(bytes.toByteArray()));
    } catch (IOException e) {
      System.err.println("Error sending binary message to client for session " + clientId + ": " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void onFailure(WebSocket webSocket, Throwable t, Response response) {
    System.err.println("❌ Error on persistent connection for session " + clientId + ": " + t.getMessage());
//    openAIWebSocketService.openAIConnections.remove(clientId);
    t.printStackTrace();
  }

  @Override
  public void onClosing(WebSocket webSocket, int code, String reason) {
    System.out.println("Persistent connection for session " + clientId + " is closing: " + reason);
  }

  @Override
  public void onClosed(WebSocket webSocket, int code, String reason) {
    System.out.println("Persistent connection for session " + clientId + " closed: " + reason);
//    openAIWebSocketService.openAIConnections.remove(clientId);
  }

  protected String getAgentSystemPrompt() {
    return masterAgentPrompt.systemPrompt();
  }
}
