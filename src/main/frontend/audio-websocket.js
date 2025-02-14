// Immediately-Invoked Function Expression to scope variables.
(function () {
  let audioSocket = null;
  let audioContext = null;
  let pcmNode = null;

  // Open WebSocket as soon as the page loads.
  function openWebSocket() {
    audioSocket = new WebSocket("ws://localhost:8080/audio");
    audioSocket.binaryType = "arraybuffer";

    audioSocket.onopen = function () {
      console.log("✅ WebSocket connection opened.");
      updateConnectionStatus("Connected");
    };

    audioSocket.onmessage = function (event) {
      if (typeof event.data === "string") {
        const response = JSON.parse(event.data);

// Fix: Correct property name ("transcript" instead of "trascript")
        console.log("📝 Received text:", response.transcript);

// Append the message correctly
        appendMessage(response.person, response.transcript);
      } else {
        console.log("🎵 Received PCM16 audio from server.");
        const pcm16ArrayBuffer = event.data;
        const wavBuffer = convertPCM16ToWav(pcm16ArrayBuffer);

        // Create a Blob and play it
        const audioBlob = new Blob([wavBuffer], { type: "audio/wav" });
        const audioUrl = URL.createObjectURL(audioBlob);
        const audioElem = new Audio(audioUrl);
        audioElem.controls = true;
        document.getElementById("audio-container").innerHTML = "";
        document.getElementById("audio-container").appendChild(audioElem);
        audioElem.play().catch(err => console.error("🎵 Playback error:", err));
      }
    };



// Convert PCM16 to WAV
    function convertPCM16ToWav(pcm16Buffer) {
      const sampleRate = 24000; // Must match the server sample rate
      const numChannels = 1;
      const bitsPerSample = 16;
      const byteRate = sampleRate * numChannels * (bitsPerSample / 8);
      const blockAlign = numChannels * (bitsPerSample / 8);
      const wavHeader = new ArrayBuffer(44);
      const view = new DataView(wavHeader);

      function writeString(view, offset, str) {
        for (let i = 0; i < str.length; i++) {
          view.setUint8(offset + i, str.charCodeAt(i));
        }
      }

      writeString(view, 0, "RIFF"); // ChunkID
      view.setUint32(4, 36 + pcm16Buffer.byteLength, true); // ChunkSize
      writeString(view, 8, "WAVE"); // Format
      writeString(view, 12, "fmt "); // Subchunk1ID
      view.setUint32(16, 16, true); // Subchunk1Size (PCM)
      view.setUint16(20, 1, true); // AudioFormat (PCM)
      view.setUint16(22, numChannels, true); // NumChannels
      view.setUint32(24, sampleRate, true); // SampleRate
      view.setUint32(28, byteRate, true); // ByteRate
      view.setUint16(32, blockAlign, true); // BlockAlign
      view.setUint16(34, bitsPerSample, true); // BitsPerSample
      writeString(view, 36, "data"); // Subchunk2ID
      view.setUint32(40, pcm16Buffer.byteLength, true); // Subchunk2Size

      return new Blob([wavHeader, pcm16Buffer], { type: "audio/wav" });
    }


    audioSocket.onerror = function (err) {
      console.error("❌ WebSocket error:", err);
      updateConnectionStatus("Error");
    };

    audioSocket.onclose = function () {
      console.log("🔌 WebSocket connection closed.");
      updateConnectionStatus("Disconnected");
    };
  }

  function updateConnectionStatus(status) {
    const statusDiv = document.getElementById("connection-status");
    if (statusDiv) {
      statusDiv.innerText = status;
    }
  }

  function float32ToPCM16(float32Array) {
    const int16Array = new Int16Array(float32Array.length);
    for (let i = 0; i < float32Array.length; i++) {
      let s = Math.max(-1, Math.min(1, float32Array[i]));
      int16Array[i] = s < 0 ? s * 0x8000 : s * 0x7FFF;
    }
    return int16Array;
  }

  async function startRecording() {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      audioContext = new AudioContext({ sampleRate: 24000 });

      // Dynamically add and register the AudioWorkletProcessor
      const workletCode = `
        class PCMProcessor extends AudioWorkletProcessor {
          process(inputs, outputs, parameters) {
            if (inputs.length > 0 && inputs[0].length > 0) {
              const channelData = inputs[0][0]; // Mono audio data
              this.port.postMessage(channelData.slice());
            }
            return true;
          }
        }
        registerProcessor("pcm-processor", PCMProcessor);
      `;

      const blob = new Blob([workletCode], { type: "application/javascript" });
      const workletUrl = URL.createObjectURL(blob);
      await audioContext.audioWorklet.addModule(workletUrl);

      pcmNode = new AudioWorkletNode(audioContext, "pcm-processor");
      pcmNode.port.onmessage = (event) => {
        const floatData = event.data;
        const pcm16Data = float32ToPCM16(floatData);
        if (audioSocket && audioSocket.readyState === WebSocket.OPEN) {
          console.log("📤 Sending PCM16 chunk of size:", pcm16Data.byteLength);
          audioSocket.send(pcm16Data.buffer);
        }
      };

      const source = audioContext.createMediaStreamSource(stream);
      source.connect(pcmNode);
      console.log("🎤 Recording started (PCM, 24kHz).");
    } catch (err) {
      console.error("❌ Error starting recording:", err);
    }
  }

  function appendMessage(sender, message) {
    const chatContainer = document.getElementById("chatHistory");
    if (!chatContainer) return;

    // Create wrapper div for alignment
    const wrapper = document.createElement("div");
    wrapper.style.width = "100%";
    wrapper.style.display = "flex";
    wrapper.style.justifyContent = sender === "Caller" ? "flex-end" : "flex-start";

    // Create message box
    const messageBox = document.createElement("div");
    messageBox.style.width = "80%";
    messageBox.style.backgroundColor = sender === "Agent" ? "#f0f0f0" : "#cce5ff";
    messageBox.style.borderRadius = "10px";
    messageBox.style.padding = "10px";
    messageBox.style.margin = "5px 0";
    messageBox.style.border = "1px solid #ddd";

    // Process message content (optional markdown support)
    let formattedContent = sender + ": " + message.replace(/\n/g, "<br/>");

    messageBox.innerHTML = formattedContent;

    // Append elements
    wrapper.appendChild(messageBox);
    chatContainer.appendChild(wrapper);

    // Auto-scroll to latest message
    chatContainer.scrollTop = chatContainer.scrollHeight;
  }

  function stopRecording() {
    if (pcmNode) {
      pcmNode.disconnect();
      pcmNode = null;
    }
    if (audioContext) {
      audioContext.close();
      audioContext = null;
    }
    console.log("🛑 Recording stopped.");
    setTimeout(() => {
      if (audioSocket) {
        console.log("🔌 Closing WebSocket for session end.");
        audioSocket.close();
        audioSocket = null;
      }
    }, 5000);
  }

  // Open WebSocket connection when the page loads
  openWebSocket();

  // Expose functions for Vaadin integration
  window.startRecording = startRecording;
  window.stopRecording = stopRecording;
})();
