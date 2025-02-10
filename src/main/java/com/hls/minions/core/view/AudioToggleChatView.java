package com.hls.minions.core.view;

import com.hls.minions.core.service.Response;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.concurrent.CompletableFuture;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * AudioToggleChatView is an audio-only chat view that uses a single toggle button.
 * When toggled on, it starts recording via the browser’s MediaRecorder API;
 * when toggled off, it stops recording and automatically submits the audio recording to the server.
 *
 * The server’s response (provided via the abstract getFuture method) is expected to include:
 *   - A text transcription (which is logged in the chat history)
 *   - A base64-encoded audio payload (which is played back on the client)
 */
public abstract class AudioToggleChatView extends VerticalLayout {

  // UI components
  private final TextField requestIdInput;
  private final Button toggleButton; // used as a toggle for starting/stopping recording
  private final Div chatHistory;

  // For rendering agent messages with Markdown (if needed)
  private final Parser markdownParser = Parser.builder().build();
  private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

  // Cached request ID (if your session or conversation needs one)
  private String cachedRequestId = null;

  // Internal flag to track if we are currently recording
  private boolean isRecording = false;

  public AudioToggleChatView() {
    // Configure the overall layout.
    setSizeFull();
    setJustifyContentMode(JustifyContentMode.CENTER);
    setAlignItems(Alignment.CENTER);

    VerticalLayout frame = new VerticalLayout();
    frame.setWidth("50%");
    frame.setPadding(true);
    frame.setAlignItems(Alignment.CENTER);

    // Request ID field (read-only)
    requestIdInput = new TextField("Request ID (Auto-generated)");
    requestIdInput.setWidth("50%");
    requestIdInput.setReadOnly(true);

    // Create the toggle button.
    toggleButton = new Button("Start Recording", event -> toggleRecording());

    // Chat history area for logging transcriptions.
    chatHistory = new Div();
    chatHistory.getStyle().set("width", "100%");
    chatHistory.getStyle().set("height", "400px");
    chatHistory.getStyle().set("overflow-y", "auto");
    chatHistory.getStyle().set("border", "1px solid #ccc");
    chatHistory.getStyle().set("padding", "10px");
    chatHistory.getStyle().set("background-color", "#fafafa");
    chatHistory.getStyle().set("display", "flex");
    chatHistory.getStyle().set("flex-direction", "column");

    HorizontalLayout controlLayout = new HorizontalLayout(toggleButton);
    controlLayout.setWidthFull();

    frame.add(requestIdInput, chatHistory, controlLayout);
    add(frame);

    // Initialize client-side global variables for recording.
    getElement().executeJs(
        "window.recordingMediaStream = null;" +
            "window.mediaRecorder = null;" +
            "window.recordedChunks = [];" +
            "window.storedRecording = null;"
    );
  }

  /**
   * Called when the toggle button is clicked.
   * If not recording, this method starts recording and changes the button text to "Stop Recording".
   * If already recording, it stops the recording and changes the button text back to "Start Recording".
   */
  private void toggleRecording() {
    if (!isRecording) {
      // Start recording.
      startRecording();
      isRecording = true;
      toggleButton.setText("Stop Recording");
    } else {
      // Stop recording and submit.
      stopRecording();
      isRecording = false;
      toggleButton.setText("Start Recording");
    }
  }

  /**
   * Starts recording audio using the browser's MediaRecorder.
   * The audio data will be stored in a global JS variable.
   */
  private void startRecording() {
    getElement().executeJs(
        // If a recording is already in progress, do nothing.
        "if (window.recordingMediaStream) { return; }" +
            "navigator.mediaDevices.getUserMedia({ audio: true }).then(function(stream) {" +
            "  window.recordingMediaStream = stream;" +
            "  window.mediaRecorder = new MediaRecorder(stream);" +
            "  window.recordedChunks = [];" +
            "  window.mediaRecorder.ondataavailable = function(e) {" +
            "    if (e.data.size > 0) {" +
            "      window.recordedChunks.push(e.data);" +
            "    }" +
            "  };" +
            "  window.mediaRecorder.start();" +
            "}).catch(function(err) {" +
            "  console.error('Could not start audio recording:', err);" +
            "});"
    );
  }

  /**
   * Stops the recording by calling the browser's MediaRecorder.stop() method.
   * When the recording stops, the onstop handler converts the audio to a base64 string,
   * stores it in a global variable, and then calls the server-side method submitRecording.
   */
  private void stopRecording() {
    getElement().executeJs(
        "if (window.mediaRecorder && window.mediaRecorder.state !== 'inactive') {" +
            "  window.mediaRecorder.onstop = function() {" +
            "    var blob = new Blob(window.recordedChunks, { type: 'audio/wav' });" +
            "    var reader = new FileReader();" +
            "    reader.onloadend = function() {" +
            "      var base64data = reader.result.split(',')[1];" +
            "      window.storedRecording = base64data;" +
            "      $0.$server.submitRecording(window.storedRecording);" +
            "    };" +
            "    reader.readAsDataURL(blob);" +
            "    window.recordedChunks = [];" +
            "    if(window.recordingMediaStream) {" +
            "      window.recordingMediaStream.getTracks().forEach(function(track) { track.stop(); });" +
            "    }" +
            "    window.recordingMediaStream = null;" +
            "    window.mediaRecorder = null;" +
            "  };" +
            "  window.mediaRecorder.stop();" +
            "} else {" +
            "  console.warn('No recording in progress to stop');" +
            "}",
        getElement()
    );
  }

  /**
   * This method is called from the client-side JavaScript (via @ClientCallable)
   * once the recording has stopped and the audio has been converted to a base64 string.
   * It logs a placeholder transcription for the user’s audio, and then sends the audio
   * to the server (via the abstract getFuture method) for processing.
   *
   * @param base64Audio The base64-encoded audio recording.
   */
  @ClientCallable
  public void submitRecording(String base64Audio) {
    // Log a placeholder for the user’s transcription.
    appendMessage("You", "[Transcription unavailable locally - pending server reply]");
    String requestId = (cachedRequestId != null) ? cachedRequestId : "";
    CompletableFuture<Response> future = getFuture(requestId, base64Audio);
    future.whenComplete((response, throwable) -> {
      getUI().ifPresent(ui -> ui.access(() -> {
        if (cachedRequestId == null || cachedRequestId.isEmpty()) {
          cachedRequestId = response.requestId();
          requestIdInput.setValue(cachedRequestId);
        }
        // Append the agent's transcription (if provided) to the chat history.
        String agentTranscript = response.response();
        if (agentTranscript != null && !agentTranscript.isEmpty()) {
          appendMessage("Agent", agentTranscript);
        }
        // If the server returns audio, play it on the client.
        String audioBase64 = response.audioBase64();
        if (audioBase64 != null && !audioBase64.isEmpty()) {
          playAudioOnClient(audioBase64);
        }
      }));
    });
  }

  /**
   * Plays an audio response on the client by creating an HTML5 Audio element with a data URL.
   *
   * @param audioBase64 The base64-encoded audio string.
   */
  private void playAudioOnClient(String audioBase64) {
    String dataUrl = "data:audio/wav;base64," + audioBase64;
    getElement().executeJs(
        "const audio = new Audio($0);" +
            "audio.play();",
        dataUrl
    );
  }

  /**
   * Appends a message bubble to the chat history. User messages are right-aligned
   * and agent messages are left-aligned.
   *
   * @param sender  "You" or "Agent"
   * @param message The message text (transcription)
   */
  private void appendMessage(String sender, String message) {
    Div wrapper = new Div();
    wrapper.getStyle().set("width", "100%");
    wrapper.getStyle().set("display", "flex");
    if ("You".equals(sender)) {
      wrapper.getStyle().set("justify-content", "flex-end");
    } else {
      wrapper.getStyle().set("justify-content", "flex-start");
    }

    Div messageBox = new Div();
    messageBox.setWidth("80%");
    String backgroundColor = "Agent".equals(sender) ? "#f0f0f0" : "#cce5ff";
    messageBox.getStyle().set("background-color", backgroundColor);
    messageBox.getStyle().set("border-radius", "10px");
    messageBox.getStyle().set("padding", "10px");
    messageBox.getStyle().set("margin", "5px 0");
    messageBox.getStyle().set("border", "1px solid #ddd");

    String formattedContent;
    if ("Agent".equals(sender)) {
      Node document = markdownParser.parse(message);
      formattedContent = htmlRenderer.render(document);
    } else {
      formattedContent = message.replaceAll("\n", "<br/>");
    }

    messageBox.getElement().setProperty("innerHTML", sender + ": " + formattedContent);
    wrapper.add(messageBox);
    chatHistory.add(wrapper);
    chatHistory.getElement().executeJs("this.scrollTop = this.scrollHeight;");
  }

  /**
   * Abstract method to be implemented by your subclass. Given a requestId and a requestDetail
   * (here, the base64-encoded audio), this method should return a CompletableFuture that yields a
   * ResponseSupplier.Response containing:
   *   - requestId(): a request identifier,
   *   - response(): the text transcription of the agent's reply, and
   *   - audioBase64(): the base64-encoded audio to play back.
   *
   * @param requestId     the request identifier (may be empty)
   * @param requestDetail the base64-encoded audio data from the user.
   * @return a CompletableFuture that yields a response.
   */
  protected abstract CompletableFuture<Response> getFuture(String requestId, String requestDetail);
}
