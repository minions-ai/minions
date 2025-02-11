package com.hls.minions.claim.tool;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

public class DocumentGeneratorTool implements Function<DocumentGeneratorTool.Request, DocumentGeneratorTool.Response> {

  private static final String OUTPUT_DIR = "generated_pdfs/";

  @Override
  public Response apply(Request request) {
    String pdfFilePath = generateClaimSummaryPDF(request.claimId(), request.policyNumber(), request.claimDetails());
    return new Response("Document Generated", pdfFilePath);
  }

  private String generateClaimSummaryPDF(String claimId, String policyNumber, String claimDetails) {
    try {
      Files.createDirectories(Paths.get(OUTPUT_DIR));
      String filePath = OUTPUT_DIR + "Claim_Summary_" + claimId + ".pdf";

      try (PDDocument document = new PDDocument()) {
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
          contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
          contentStream.beginText();
          contentStream.newLineAtOffset(100, 700);
          contentStream.showText("Claim Summary Report");
          contentStream.endText();

          contentStream.setFont(PDType1Font.HELVETICA, 12);
          contentStream.beginText();
          contentStream.newLineAtOffset(100, 650);
          contentStream.showText("Claim ID: " + claimId);
          contentStream.newLineAtOffset(0, -20);
          contentStream.showText("Policy Number: " + policyNumber);
          contentStream.newLineAtOffset(0, -20);
          contentStream.showText("Claim Details: " + claimDetails);
          contentStream.endText();
        }

        document.save(filePath);
      }
      return filePath;
    } catch (IOException e) {
      e.printStackTrace();
      return "Error generating PDF";
    }
  }

  public record Request(String claimId, String policyNumber, String claimDetails) {}
  public record Response(String status, String generatedDocumentUrl) {}
}
