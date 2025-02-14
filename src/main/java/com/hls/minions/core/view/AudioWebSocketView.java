package com.hls.minions.core.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

//@Route("")
//@JavaScript("./pcm-processor.js")
@JavaScript("./audio-websocket.js")
public class AudioWebSocketView extends VerticalLayout {

  private final Div transcriptionDiv;
  private final Div connectionStatusDiv;

  public AudioWebSocketView() {
    Button startButton = new Button("🎤 Start Recording", event -> startAudio());
    // Optionally, include a manual "End Session" button for session management
    Button endSessionButton = new Button("🔌 End Session", event -> endSession());

    transcriptionDiv = new Div();
    transcriptionDiv.setText("Transcription will appear here...");
    transcriptionDiv.setId("transcription-container");

    connectionStatusDiv = new Div();
    connectionStatusDiv.setText("Not connected");
    connectionStatusDiv.setId("connection-status");

    Div audioContainer = new Div();
    audioContainer.setId("audio-container");

    add(startButton, endSessionButton, connectionStatusDiv, transcriptionDiv, audioContainer);

    UI.getCurrent().getPage().executeJs("async function loadAudioWorkletModule() {\n"
        + "  try {\n"
        + "    // Assuming your audio context is already created\n"
        + "    await audioContext.audioWorklet.addModule(\"frontend://pcm-processor.js\");\n"
        + "    console.log(\"✅ pcm-processor module loaded successfully.\");\n"
        + "  } catch (error) {\n"
        + "    console.error(\"❌ Failed to load pcm-processor module:\", error);\n"
        + "  }\n"
        + "}");
  }

  private void startAudio() {
    UI.getCurrent().getPage().executeJs("window.startRecording();");
  }

  private void endSession() {
    UI.getCurrent().getPage().executeJs("window.endSession && window.endSession();");
  }
}
