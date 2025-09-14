package il.ac.hit.project.main.model.report.external;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Responsible for generating PDF documents using the Apache PDFBox library.
 * This class provides functionality to create and save a PDF document with a title and list of content lines.
 */
public class PDFBoxReportGenerator {


    /**
     * Generates a PDF document with the given title and content.
     *
     * @param title non-null title of the report
     * @param content non-null list of strings to display in the report
     * @param filePath non-null destination file path; may overwrite if the file exists
     * @throws IOException if an IO error occurs while writing the PDF document
     */
    public void createDocument(String title, List<String> content, String filePath) throws IOException {
        // Create a new empty document
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)){
                document.addPage(page);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.setLeading(14.5f); // Set leading for line spacing
                contentStream.newLineAtOffset(50, 750); // Set the starting position

                // Write the title
                contentStream.showText(title);
                contentStream.newLine();
                contentStream.newLine();

                // Write the content lines
                PDType0Font font = PDType0Font.load(document, new File("fonts/arial.ttf"));
                contentStream.setFont(font, 12);
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
