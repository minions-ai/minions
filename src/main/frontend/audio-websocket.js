// Immediately-Invoked Function Expression to scope our variables.
(function () {
  let mediaRecorder = null;
  let audioSocket = null;

  // Opens a WebSocket connection and sets up handlers.
  function openWebSocket() {
    audioSocket = new WebSocket("ws://localhost:8080/audio");
    audioSocket.binaryType = 'blob';

    audioSocket.onopen = function() {
      console.log("WebSocket connection opened.");
    };

    // When processed audio is received from the server, create an audio element in the UI and play it.
    audioSocket.onmessage = function(event) {
      if (event.data) {
        const audioBlob = event.data;
        const audioUrl = URL.createObjectURL(audioBlob);
        console.log("Received processed audio from server.");

        // Get the container element from the UI.
        const container = document.getElementById("audio-container");
        // Clear any previous audio elements.
        container.innerHTML = "";

        // Create an audio element with controls.
        const audioElem = document.createElement("audio");
        audioElem.src = audioUrl;
        audioElem.controls = true;
        container.appendChild(audioElem);

        // Optionally, auto-play the audio.
        audioElem.play().then(() => {
          console.log("Playing processed audio.");
        }).catch(err => {
          console.error("Error auto-playing processed audio:", err);
        });
      }
    };

    audioSocket.onerror = function(err) {
      console.error("WebSocket error:", err);
    };

    audioSocket.onclose = function() {
      console.log("WebSocket connection closed.");
    };
  }

  // Helper function to check if a Blob is all zeros.
  function isBlobValid(blob, callback) {
    const reader = new FileReader();
    reader.onload = function(e) {
      const buffer = e.target.result;
      const view = new Uint8Array(buffer);
      let valid = false;
      for (let i = 0; i < view.length; i++) {
        if (view[i] !== 0) {
          valid = true;
          break;
        }
      }
      callback(valid);
    };
    reader.onerror = function(e) {
      console.error("Error reading blob:", e);
      callback(false);
    };
    reader.readAsArrayBuffer(blob);
  }

  // Start capturing audio and open WebSocket connection.
  async function startRecording() {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      openWebSocket();
      mediaRecorder = new MediaRecorder(stream, { mimeType: "audio/webm;codecs=opus" });

      mediaRecorder.ondataavailable = function(event) {
        if (event.data && event.data.size > 0 && audioSocket.readyState === WebSocket.OPEN) {
          const blob = event.data;
          isBlobValid(blob, function(valid) {
            if (valid) {
              console.log("Sending recorded audio chunk to server.");
              audioSocket.send(blob);
            } else {
              console.warn("Invalid audio chunk received (all zeros), not sending.");
            }
          });
        }
      };

      mediaRecorder.start(1000);
      console.log("Recording started.");
    } catch (err) {
      console.error("Error accessing microphone:", err);
    }
  }
  // Stop recording, send final signal, and close WebSocket.
  function stopRecording() {
    if (mediaRecorder) {
      mediaRecorder.stop();
      mediaRecorder = null;
      console.log("Recording stopped.");
      if (audioSocket && audioSocket.readyState === WebSocket.OPEN) {
        audioSocket.send("end");
      }
    }
    setTimeout(() => {
      if (audioSocket) {
        audioSocket.close();
        audioSocket = null;
      }
    }, 2000);
  }

  window.startRecording = startRecording;
  window.stopRecording = stopRecording;
})();
