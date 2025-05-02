package com.minionsai.patient.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/pdf")
public class PdfController {

  private static final String OUTPUT_DIR = "/Users/vahid/workspace/hyperlogic/hyperlogic-minions/generated_pdfs";

  @GetMapping("/download/{filename}")
  public ResponseEntity<Resource> downloadPdf(@PathVariable String filename) throws IOException {
    // Construct the path to the PDF file
    Path filePath = Path.of(OUTPUT_DIR + filename);
    File pdfFile = filePath.toFile();

    // Check if the file exists
    if (!pdfFile.exists()) {
      return ResponseEntity.notFound().build();
    }

    // Read file into a ByteArrayResource
    ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(filePath));

    // Set headers so the browser will messages a download
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    // For inline display, use `inline`
    // For forced download, use `attachment`
    headers.setContentDisposition(
        ContentDisposition.builder("attachment")
            .filename(pdfFile.getName())
            .build()
    );

    return new ResponseEntity<>(resource, headers, HttpStatus.OK);
  }
}
