package com.minionsai.core.view;

import com.minionsai.core.service.ResponseSupplier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.concurrent.CompletableFuture;

public abstract class ChatView extends VerticalLayout {

  private final TextArea userInput;
  private final TextField requestIdInput;
  private final Button sendButton;
  private final Div chatHistory; // HTML container for chat history

  private String cachedRequestId = null; // Stores the request ID for continuity

  // Markdown parser and renderer for agent entries.
  private final Parser markdownParser = Parser.builder().build();
  private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

  public ChatView() {
    setSizeFull();
    setJustifyContentMode(JustifyContentMode.CENTER);
    setAlignItems(Alignment.CENTER);

    VerticalLayout frame = new VerticalLayout();
    frame.setWidth("50%");
    frame.setPadding(true);
    frame.setAlignItems(Alignment.CENTER);

    requestIdInput = new TextField("Request ID (Auto-generated)");
    requestIdInput.setWidth("50%");
    requestIdInput.setReadOnly(true); // Prevent manual edits

    userInput = new TextArea("Enter request details"); // Multi-line input
    userInput.setWidthFull();
    userInput.setHeight("150px"); // Increased size for better usability

    sendButton = new Button("Send", event -> sendRequest());

    // Create a scrollable Div for the chat history
    chatHistory = new Div();
    chatHistory.getStyle().set("width", "100%");
    chatHistory.getStyle().set("height", "400px");
    chatHistory.getStyle().set("overflow-y", "auto");
    chatHistory.getStyle().set("border", "1px solid #ccc");
    chatHistory.getStyle().set("padding", "10px");
    chatHistory.getStyle().set("background-color", "#fafafa");
    // Use flex layout for vertical stacking
    chatHistory.getStyle().set("display", "flex");
    chatHistory.getStyle().set("flex-direction", "column");

    HorizontalLayout inputLayout = new HorizontalLayout(userInput, sendButton);
    inputLayout.setWidthFull();

    frame.add(requestIdInput, chatHistory, inputLayout);
    add(frame);
  }

  private void sendRequest() {
    String inputRequest = userInput.getValue().trim();
    if (inputRequest.isEmpty()) {
      return; // Don't send empty requests
    }

    userInput.clear();
    // Append the user's message with custom styling
    appendMessage("You", inputRequest);

    // Use cached request ID if available, otherwise pass an empty string
    String requestId = (cachedRequestId != null) ? cachedRequestId : "";

    // Call service asynchronously and attach a completion callback
    CompletableFuture<ResponseSupplier.Response> future = getFuture(requestId, inputRequest);

    future.whenComplete((response, throwable) -> {
      getUI().ifPresent(ui -> ui.access(new Command() {
        @Override
        public void execute() {
          if (cachedRequestId == null || cachedRequestId.isEmpty()) {
            cachedRequestId = response.requestId();
            requestIdInput.setValue(cachedRequestId);
          }
          // Append agent's message with custom styling and markdown processing
          appendMessage("Agent", response.response());
        }
      }));
    });
  }

  /**
   * Appends a message to the chat history.
   * Each message is wrapped in a flex container that aligns the box to the left for agent messages
   * and to the right for user messages. The message box covers 80% of the container's width.
   * For agent messages, Markdown is converted to HTML before rendering.
   *
   * @param sender  "You" for user messages and "Agent" for agent responses.
   * @param message The message text.
   */
  private void appendMessage(String sender, String message) {
    // Create a wrapper Div to control alignment using flex layout
    Div wrapper = new Div();
    wrapper.getStyle().set("width", "100%");
    wrapper.getStyle().set("display", "flex");
    // Align to right for user messages, left for agent messages
    if (sender.equals("You")) {
      wrapper.getStyle().set("justify-content", "flex-end");
    } else {
      wrapper.getStyle().set("justify-content", "flex-start");
    }

    // Create the message box with a fixed width of 80%
    Div messageBox = new Div();
    messageBox.setWidth("80%");
    // Choose background color based on sender
    String backgroundColor = sender.equals("Agent") ? "#f0f0f0" : "#cce5ff";
    messageBox.getStyle().set("background-color", backgroundColor);
    messageBox.getStyle().set("border-radius", "10px");
    messageBox.getStyle().set("padding", "10px");
    messageBox.getStyle().set("margin", "5px 0");
    messageBox.getStyle().set("border", "1px solid #ddd");

    // Process the message content
    String formattedContent;
    if (sender.equals("Agent")) {
      // Parse the markdown and convert it to HTML
      Node document = markdownParser.parse(message);
      formattedContent = htmlRenderer.render(document);
    } else {
      // For user messages, preserve line breaks
      formattedContent = message.replaceAll("\n", "<br/>");
    }

    // Set the inner HTML of the message box to include the sender and formatted message
    messageBox.getElement().setProperty("innerHTML", sender + ": " + formattedContent);

    // Add the message box to the wrapper and then to the chat history
    wrapper.add(messageBox);
    chatHistory.add(wrapper);

    // Scroll to the bottom of the chat history after adding a new message
    chatHistory.getElement().executeJs("this.scrollTop = this.scrollHeight;");
  }

  protected abstract CompletableFuture<ResponseSupplier.Response> getFuture(String requestId, String requestDetail);
}
