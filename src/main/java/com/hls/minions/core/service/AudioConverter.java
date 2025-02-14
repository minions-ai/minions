package com.hls.minions.core.service;

import lombok.extern.slf4j.Slf4j;
import ws.schild.jave.*;
import java.io.*;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

@Slf4j public class AudioConverter {

  public static byte[] convertWebMtoPCM(byte[] webmBytes) throws Exception {
    // Create temporary input and output files
    File inputFile = File.createTempFile("input", ".webm");
    File outputFile = File.createTempFile("output", ".wav");

    try {
      // Write byte array to temp file
      try (FileOutputStream fos = new FileOutputStream(inputFile)) {
        fos.write(webmBytes);
      }

      // Configure audio attributes for PCM16 (24kHz)
      AudioAttributes audio = new AudioAttributes();
      audio.setCodec("pcm_s16le"); // PCM16
      audio.setSamplingRate(24000); // 24kHz
      audio.setChannels(1); // Mono

      EncodingAttributes attrs = new EncodingAttributes();
      attrs.setOutputFormat("wav");
      attrs.setAudioAttributes(audio);

      // Convert WebM to PCM16
      Encoder encoder = new Encoder();
      encoder.encode(new MultimediaObject(inputFile), outputFile, attrs);

      // Read the output file into a byte array
      return java.nio.file.Files.readAllBytes(outputFile.toPath());
    }catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      // Cleanup temp files
      inputFile.delete();
      outputFile.delete();
    }
    return null;
  }

  public static void main(String[] args) throws Exception {
    byte[] webmAudio = java.nio.file.Files.readAllBytes(new File("sample.webm").toPath());
    byte[] pcmAudio = convertWebMtoPCM(webmAudio);
    System.out.println("Conversion successful, PCM16 byte array length: " + pcmAudio.length);
  }
}

