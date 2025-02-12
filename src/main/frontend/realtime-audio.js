window.startAudioConversation = async function() {
  try {
    // 1. Fetch an ephemeral API token from your server.
    // Your server should expose an endpoint (e.g. /session) that uses your standard API key
    // to mint an ephemeral token via OpenAI's REST API.
    const tokenResponse = await fetch("/session");
    const tokenData = await tokenResponse.json();
    const ephemeralKey = tokenData.client_secret.value;  // Ephemeral token

    // 2. Create a new RTCPeerConnection.
    const pc = new RTCPeerConnection();
    window.pc = pc; // Store for later cleanup

    // 3. Create an audio element to play the remote audio stream.
    let audioEl = document.querySelector("#audio-container audio");
    if (!audioEl) {
      audioEl = document.createElement("audio");
      audioEl.autoplay = true;
      document.getElementById("audio-container").appendChild(audioEl);
    }

    // 4. Set up the remote track handler.
    pc.ontrack = event => {
      // Attach the remote audio stream to the audio element.
      audioEl.srcObject = event.streams[0];
    };

    // 5. Capture local audio from the user's microphone.
    const localStream = await navigator.mediaDevices.getUserMedia({ audio: true });
    localStream.getTracks().forEach(track => {
      pc.addTrack(track, localStream);
    });

    // 6. Set up a data channel for sending/receiving realtime events.
    const dc = pc.createDataChannel("oai-events");
    dc.onmessage = e => {
      try {
        const realtimeEvent = JSON.parse(e.data);
        console.log("Received event:", realtimeEvent);
      } catch (err) {
        console.error("Error parsing data channel message:", err);
      }
    };

    // 7. Create an SDP offer.
    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);

    // 8. Send the offer to the OpenAI Realtime API.
    // The API URL and model ID are provided by OpenAI.
    const baseUrl = "https://api.openai.com/v1/realtime";
    const model = "gpt-4o-realtime-preview-2024-12-17";
    const sdpResponse = await fetch(`${baseUrl}?model=${model}`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${ephemeralKey}`,
        "Content-Type": "application/sdp"
      },
      body: offer.sdp
    });

    if (!sdpResponse.ok) {
      throw new Error("Failed to obtain SDP answer from OpenAI Realtime API");
    }

    const answerSDP = await sdpResponse.text();
    const answer = {
      type: "answer",
      sdp: answerSDP
    };

    // 9. Set the remote description to complete the handshake.
    await pc.setRemoteDescription(answer);
    console.log("WebRTC connection established with OpenAI Realtime API.");
  } catch (error) {
    console.error("Error starting audio conversation:", error);
  }
};

window.stopAudioConversation = function() {
  // Close the peer connection if it exists.
  if (window.pc) {
    window.pc.close();
    window.pc = null;
  }
  console.log("Audio conversation stopped.");
};

// Optional: Initialize the audio container by ensuring an <audio> element is present.
document.addEventListener('DOMContentLoaded', () => {
  const container = document.getElementById("audio-container");
  if (!container.querySelector("audio")) {
    const audioEl = document.createElement("audio");
    audioEl.autoplay = true;
    container.appendChild(audioEl);
  }
});
