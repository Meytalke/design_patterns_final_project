package model.report.external;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.List;

/**
 * Responsible for generating PDF documents using the Apache PDFBox library.
 * This class provides functionality to create and save a PDF document with a title and list of content lines.
 */
public class PdfBoxReportGenerator {

    /**
     * Generates a PDF document with the given title and content.
     */
    public void createDocument(String title, List<String> content, String filePath) throws IOException {
        // Create a new empty document
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.setLeading(14.5f); // Set leading for line spacing
                contentStream.newLineAtOffset(50, 750); // Set the starting position

                // Write the title
                contentStream.showText(title);
                contentStream.newLine();
                contentStream.newLine();

                // Write the content lines
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                for (String line : content) {
                    contentStream.showText(line);
                    contentStream.newLine();
                }

                contentStream.endText();
            }

            // Save the document to the specified file path
            document.save(filePath);
            System.out.println("PDF document generated successfully at: " + filePath);
        } catch (IOException e) {
            System.err.println("Error generating PDF document: " + e.getMessage());
            throw e;
        }
    }
}
