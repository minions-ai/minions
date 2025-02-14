class PCMProcessor extends AudioWorkletProcessor {
  process(inputs, outputs, parameters) {
    // Assume mono input: use the first channel of the first input.
    if (inputs.length > 0 && inputs[0].length > 0) {
      const inputChannel = inputs[0][0];
      // Post the Float32Array of PCM samples to the main thread.
      // You might choose to send a copy if needed.
      this.port.postMessage(new Float32Array(inputChannel));
    }
    // Return true to keep processing.
    return true;
  }
}

registerProcessor("pcm-processor", PCMProcessor);
