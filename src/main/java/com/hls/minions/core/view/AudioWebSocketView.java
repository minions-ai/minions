package com.hls.minions.core.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
@JavaScript("./audio-websocket.js")
public abstract class AudioWebSocketView extends ChatView {

  public AudioWebSocketView() {
    Button startButton = new Button("Start Audio Conversation", event -> startAudio());
    Button stopButton = new Button("Stop Audio Conversation", event -> stopAudio());
    Div audioContainer = new Div();
    audioContainer.setId("audio-container");

    add(startButton, stopButton, audioContainer);
  }

  private void startAudio() {
    // Call the client-side function to start audio capture and open the WebSocket
    UI.getCurrent().getPage().executeJs("window.startRecording();");
  }

  private void stopAudio() {
    // Call the client-side function to stop audio capture and close the WebSocket
    UI.getCurrent().getPage().executeJs("window.stopRecording();");
  }
}
