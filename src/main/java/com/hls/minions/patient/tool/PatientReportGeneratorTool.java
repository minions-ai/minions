package com.hls.minions.patient.report;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

/**
 * This tool generates a stylish PDF patient report based on Q&A data.
 * The recommendation section is wrapped so it doesn't exceed the page width.
 */
public class PatientReportGeneratorTool implements Function<PatientReportGeneratorTool.Request, PatientReportGeneratorTool.Response> {

  private static final String OUTPUT_DIR = "generated_pdfs/";

  @Override
  public Response apply(Request request) {
    String pdfFilePath = generatePatientReportPDF(request);
    return new Response("Document Generated Successfully.", getPDFURL(pdfFilePath));
  }

  private String getPDFURL(String pdfFilePath) {
    return "/pdf/download/" + pdfFilePath;
  }

  private String generatePatientReportPDF(Request req) {
    try {
      Files.createDirectories(Paths.get(OUTPUT_DIR));
      // Construct a unique file name for the PDF
      String report_name = "Patient_Report_" + sanitizeFileName(req.patientName()) + ".pdf";
      String filePath = OUTPUT_DIR + report_name;

      try (PDDocument document = new PDDocument()) {
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

          // === HEADER SECTION ===
          contentStream.addRect(0, page.getMediaBox().getHeight() - 80, page.getMediaBox().getWidth(), 80);
          contentStream.setNonStrokingColor(79 / 255f, 129 / 255f, 189 / 255f); // a nice blue shade
          contentStream.fill();

          // Title Text
          contentStream.beginText();
          contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
          contentStream.setNonStrokingColor(1f, 1f, 1f); // White text
          contentStream.newLineAtOffset(50, page.getMediaBox().getHeight() - 50);
          contentStream.showText("Patient Health Assessment Report");
          contentStream.endText();

          // Reset color to black for normal text
          contentStream.setNonStrokingColor(0, 0, 0);

          float startY = page.getMediaBox().getHeight() - 120;
          float margin = 50;
          float leading = 18;

          // === PATIENT DETAILS SECTION ===
          contentStream.beginText();
          contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
          contentStream.newLineAtOffset(margin, startY);
          contentStream.showText("Patient Information");
          contentStream.endText();

          startY -= leading;

          contentStream.beginText();
          contentStream.setFont(PDType1Font.HELVETICA, 12);
          contentStream.newLineAtOffset(margin, startY);
          contentStream.showText("Name: " + req.patientName());
          contentStream.newLineAtOffset(0, -leading);
          contentStream.showText("Date of Birth: " + req.dateOfBirth());
          contentStream.newLineAtOffset(0, -leading);
          contentStream.showText("Location: " + req.currentLocation());
          contentStream.newLineAtOffset(0, -leading);
          contentStream.showText("Emergency Contact: " + req.emergencyContact());
          contentStream.newLineAtOffset(0, -leading);
          contentStream.showText("Known Conditions: " + req.knownMedicalConditions());
          contentStream.newLineAtOffset(0, -leading);
          contentStream.showText("Allergies: " + req.allergies());
          contentStream.endText();

          startY -= (leading * 7);

          // === MEDICATIONS SECTION ===
          contentStream.beginText();
          contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
          contentStream.newLineAtOffset(margin, startY);
          contentStream.showText("Medications");
          contentStream.endText();

          startY -= leading;

          contentStream.beginText();
          contentStream.setFont(PDType1Font.HELVETICA, 12);
          contentStream.newLineAtOffset(margin, startY);
          if (req.currentMedications() != null && !req.currentMedications().isEmpty()) {
            contentStream.showText("Current Medications:");
            for (String med : req.currentMedications()) {
              contentStream.newLineAtOffset(0, -leading);
              contentStream.showText("- " + med);
            }
          } else {
            contentStream.showText("Current Medications: None");
          }
          contentStream.endText();

          // Adjust startY based on how many lines we used
          int linesUsedForCurrentMed = (req.currentMedications() == null || req.currentMedications().isEmpty()) ? 1 : req.currentMedications().size();
          startY -= (leading * (1 + linesUsedForCurrentMed + 1));

          // === ASSESSMENT RESPONSES SECTION ===
          contentStream.beginText();
          contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
          contentStream.newLineAtOffset(margin, startY);
          contentStream.showText("Assessment Q&A");
          contentStream.endText();

          startY -= leading;

          if (req.assessmentResponses() != null && !req.assessmentResponses().isEmpty()) {
            float tempY = startY;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(margin, tempY);

            for (String response : req.assessmentResponses()) {
              contentStream.showText("- " + response);
              contentStream.newLineAtOffset(0, -leading);
              tempY -= leading;
            }
            contentStream.endText();
            startY = tempY - leading;
          } else {
            startY -= leading;
          }

          // === RECOMMENDATIONS SECTION ===
          contentStream.beginText();
          contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
          contentStream.newLineAtOffset(margin, startY);
          contentStream.showText("Recommended Action");
          contentStream.endText();

          startY -= (leading * 2);

          // Wrap recommendation text
          startY = wrapText(contentStream, PDType1Font.HELVETICA, 12, req.recommendation(), margin, startY, page.getMediaBox().getWidth() - (margin * 2), leading);
        }

        // Save the document
        document.save(filePath);
      }
      return report_name;
    } catch (IOException e) {
      e.printStackTrace();
      return "Error generating PDF";
    }
  }

  /**
   * Writes text with word-wrapping at a specified width.
   * Returns the new Y position after writing.
   */
  private float wrapText(PDPageContentStream contentStream,
      PDFont font,
      float fontSize,
      String text,
      float startX,
      float startY,
      float maxWidth,
      float leading) throws IOException {
    if (text == null || text.isEmpty()) {
      return startY;
    }

    String[] words = text.split("\\s+");
    StringBuilder line = new StringBuilder();

    contentStream.beginText();
    contentStream.setFont(font, fontSize);
    contentStream.newLineAtOffset(startX, startY);

    for (int i = 0; i < words.length; i++) {
      String word = words[i];
      String testLine = (line.length() == 0) ? word : line + " " + word;
      float size = font.getStringWidth(testLine) / 1000 * fontSize;
      if (size > maxWidth) {
        // Write the current line
        contentStream.showText(line.toString());
        // Move to a new line
        contentStream.newLineAtOffset(0, -leading);
        // Reset line with the new word
        line = new StringBuilder(word);
      } else {
        line = new StringBuilder(testLine);
      }
    }
    // Write the remaining line
    contentStream.showText(line.toString());
    contentStream.endText();

    // Calculate how many lines total were used
    int numLines = (int) Math.ceil((float) (font.getStringWidth(text) / 1000 * fontSize) / maxWidth);
    // Add one because we wrote the last line as well
    if (numLines < 1) {
      numLines = 1;
    }
    // Return the new Y position after all lines
    // Leading * (numLines - 1) because we start at startY.
    float newY = startY - (leading * (numLines - 1));
    return newY - leading * 2; // Extra spacing
  }

  private String sanitizeFileName(String input) {
    // Replace spaces and special chars
    return input.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_\\-]", "");
  }

  /**
   * Request record containing all data needed to generate the PDF.
   */
  public record Request(
      String patientName,
      String dateOfBirth,
      String currentLocation,
      String emergencyContact,
      String knownMedicalConditions,
      String allergies,
      List<String> currentMedications,
      List<String> assessmentResponses,
      String recommendation
  ) {}

  /**
   * Response record providing status and file path to the generated PDF.
   */
  public record Response(String status, String generatedDocumentUrl) {}
}
