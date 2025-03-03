package com.minionsai.patient.service;

import java.io.File;
import java.nio.file.Files;
import org.springframework.stereotype.Service;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

@Service
public class WebMToMp3Converter {

  public byte[] convertWebMToMp3(byte[] webmData) throws Exception {
    // Create temp input file
    File inputFile = File.createTempFile("input", ".webm");
    // Write data to the temp file created by File.createTempFile
    Files.write(inputFile.toPath(), webmData);

    // Create temp output file
    File outputFile = File.createTempFile("output", ".mp3");

    try {
      AudioAttributes audio = new AudioAttributes();
      audio.setCodec("libmp3lame"); // Use LAME MP3 encoder
      audio.setBitRate(128000); // 128kbps
      audio.setChannels(2);
      audio.setSamplingRate(44100);

      EncodingAttributes attrs = new EncodingAttributes();
      attrs.setInputFormat("webm");
      // Depending on your JAVE2 version, use setFormat() if required
      attrs.setOutputFormat("mp3");
      attrs.setAudioAttributes(audio);

      Encoder encoder = new Encoder();
      encoder.encode(new MultimediaObject(inputFile), outputFile, attrs);

      // Read MP3 file into byte array
      byte[] mp3Data = Files.readAllBytes(outputFile.toPath());
      return mp3Data;
    } catch (Exception e) {
      e.printStackTrace();
      throw e;  // or handle the exception as needed
    } finally {
      // Cleanup temporary files
      inputFile.delete();
      outputFile.delete();
    }
  }
}
