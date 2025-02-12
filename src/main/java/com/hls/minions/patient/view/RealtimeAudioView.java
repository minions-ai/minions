package com.hls.minions.patient.view;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("/realtime")
@JavaScript("./realtime-audio.js")
public class RealtimeAudioView extends VerticalLayout {

  public RealtimeAudioView() {
    Button startButton = new Button("Start Audio Conversation", event -> startAudio());
    Button stopButton = new Button("Stop Audio Conversation", event -> stopAudio());

    Div audioContainer = new Div();
    audioContainer.setId("audio-container");

    add(startButton, stopButton, audioContainer);
  }

  private void startAudio() {
    // Call the client-side function that establishes a WebRTC connection
    UI.getCurrent().getPage().executeJs("window.startAudioConversation();");
  }

  private void stopAudio() {
    // Call the client-side function to tear down the WebRTC connection
    UI.getCurrent().getPage().executeJs("window.stopAudioConversation();");
  }
}
