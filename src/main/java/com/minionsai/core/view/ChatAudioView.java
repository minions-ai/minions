package com.minionsai.core.view;


import com.minionsai.core.service.ResponseSupplier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.concurrent.CompletableFuture;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

@JavaScript("./audio-websocket.js")
public abstract class ChatAudioView extends VerticalLayout {

  // Text-based chat components
//  private final TextArea userInput;
//  private final TextField requestIdInput;
//  private final Button sendButton;
  private final Div chatHistory;

  // Audio-based components
  private final Button startRecordingButton;
  private final Button endSessionButton;
  //  private final Div transcriptionDiv;
  private final Div connectionStatusDiv;
  private final Div audioContainer;
  private final Div titleDiv;
  private final Parser markdownParser = Parser.builder().build();
  private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();
  private String cachedRequestId = null;

  public ChatAudioView() {
    setSizeFull();
    setJustifyContentMode(JustifyContentMode.CENTER);
    setAlignItems(Alignment.CENTER);

    VerticalLayout frame = new VerticalLayout();
    frame.setWidth("50%");
    frame.setPadding(true);
    frame.setAlignItems(Alignment.CENTER);

    // Send button
//    sendButton = new Button("Send", event -> sendRequest());

    // Chat history display
    chatHistory = new Div();
    chatHistory.setId("chatHistory");
    chatHistory.getStyle().set("width", "100%");
    chatHistory.getStyle().set("height", "400px");
    chatHistory.getStyle().set("overflow-y", "auto");
    chatHistory.getStyle().set("border", "1px solid #ccc");
    chatHistory.getStyle().set("padding", "10px");
    chatHistory.getStyle().set("background-color", "#fafafa");
    chatHistory.getStyle().set("display", "flex");
    chatHistory.getStyle().set("flex-direction", "column");

    // Horizontal layout for text input
/*    HorizontalLayout inputLayout = new HorizontalLayout(userInput, sendButton);
    inputLayout.setWidthFull();*/

    // Audio components
    startRecordingButton = new Button("🎤 Start Recording", event -> startAudio());
    endSessionButton = new Button("🔌 End Session", event -> endSession());

//    transcriptionDiv = new Div();
//    transcriptionDiv.setText("Transcription will appear here...");
//    transcriptionDiv.setId("transcription-container");

    titleDiv= new Div();
    titleDiv.setText("Mental Health Crisis Assistant");
    titleDiv.addClassName("title-text"); // Apply the CSS class
    titleDiv.getStyle()
        .set("font-size", "24px")
        .set("font-weight", "bold")
        .set("color", "#2c3e50")
        .set("text-align", "center")
        .set("margin-top", "20px");

    connectionStatusDiv = new Div();
    connectionStatusDiv.setText("Not connected");
    connectionStatusDiv.setId("connection-status");

    audioContainer = new Div();
    audioContainer.setId("audio-container");
    audioContainer.getStyle().set("display", "none"); // Hides the audio container

    // Layout sections
    frame.add(chatHistory);
    add(titleDiv,frame, startRecordingButton, endSessionButton, connectionStatusDiv, audioContainer);

    // Load Audio Worklet Module
    UI.getCurrent().getPage().executeJs("""
            async function loadAudioWorkletModule() {
                try {
                    await audioContext.audioWorklet.addModule("frontend://pcm-processor.js");
                    console.log("✅ pcm-processor module loaded successfully.");
                } catch (error) {
                    console.error("❌ Failed to load pcm-processor module:", error);
                }
            }
        """);
  }


  /**
   * Appends a message to the chat history.
   */
  private void appendMessage(String sender, String message) {
    Div wrapper = new Div();
    wrapper.getStyle().set("width", "100%");
    wrapper.getStyle().set("display", "flex");
    wrapper.getStyle().set("justify-content", sender.equals("You") ? "flex-end" : "flex-start");

    Div messageBox = new Div();
    messageBox.setWidth("80%");
    String backgroundColor = sender.equals("Agent") ? "#f0f0f0" : "#cce5ff";
    messageBox.getStyle().set("background-color", backgroundColor);
    messageBox.getStyle().set("border-radius", "10px");
    messageBox.getStyle().set("padding", "10px");
    messageBox.getStyle().set("margin", "5px 0");
    messageBox.getStyle().set("border", "1px solid #ddd");

    String formattedContent;
    if (sender.equals("Agent")) {
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
   * Starts audio recording.
   */
  private void startAudio() {
    UI.getCurrent().getPage().executeJs("window.startRecording();");
  }

  /**
   * Ends audio session.
   */
  private void endSession() {
    UI.getCurrent().getPage().executeJs("window.endSession && window.endSession();");
  }

  /**
   * Abstract method to fetch responses for chat requests.
   */
  protected abstract CompletableFuture<ResponseSupplier.Response> getFuture(String requestId, String requestDetail);
}

