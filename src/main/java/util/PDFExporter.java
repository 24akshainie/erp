package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.util.List;

public class PDFExporter {

    /**
     * Export simple text to PDF.
     */
    public static void exportText(String fileName, String title, String content) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph pTitle = new Paragraph(title, titleFont);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            pTitle.setSpacingAfter(20);
            document.add(pTitle);

            // Content
            Font textFont = new Font(Font.FontFamily.HELVETICA, 12);
            Paragraph pContent = new Paragraph(content, textFont);
            pContent.setAlignment(Element.ALIGN_LEFT);
            document.add(pContent);

            document.close();
            System.out.println("[PDFExporter] PDF created: " + fileName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Export a table to PDF.
     */
    public static void exportTable(String fileName, String title,
                                   List<String> headers, List<List<String>> rows) {
        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph pTitle = new Paragraph(title, titleFont);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            pTitle.setSpacingAfter(20);
            document.add(pTitle);

            // Table
            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);

            // Headers
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            // Rows
            for (List<String> row : rows) {
                for (String cellData : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(cellData, cellFont));
                    cell.setPadding(6);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
            System.out.println("[PDFExporter] Table PDF created: " + fileName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
