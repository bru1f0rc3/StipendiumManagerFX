package ru.demo.demo2.util;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PdfExporter {
    private static final String FONT_PATH = "src/main/resources/fonts/arial.ttf";

    public static void exportToPDF(List<String[]> data, String[] headers, String title, Stage stage) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить PDF файл");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF файлы", "*.pdf"));

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            Font font = FontFactory.getFont(FONT_PATH, "cp1251", BaseFont.EMBEDDED, 10);
            document.open();

            Font titleFont = FontFactory.getFont(FONT_PATH, "cp1251", BaseFont.EMBEDDED, 14, Font.BOLD);
            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(titleParagraph);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);

            PdfPCell header;
            for (String headerText : headers) {
                header = new PdfPCell();
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header.setBorderWidth(1);
                header.setPhrase(new Phrase(headerText, font));
                table.addCell(header);
            }

            for (String[] row : data) {
                for (String cell : row) {
                    PdfPCell cellData = new PdfPCell();
                    cellData.setPhrase(new Phrase(cell != null ? cell : "", font));
                    table.addCell(cellData);
                }
            }

            document.add(table);
            document.close();
        }
    }
}