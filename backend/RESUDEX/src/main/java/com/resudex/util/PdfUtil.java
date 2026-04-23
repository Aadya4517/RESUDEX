package com.resudex.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import com.resudex.model.ResumeScore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PdfUtil {

    public static String extractText(InputStream inputStream) {
        try {
            PDDocument document = PDDocument.load(inputStream);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text;
        } catch (Exception e) {
            return "";
        }
    }

    public static byte[] generateShortlistReport(ResumeScore score, String candidateName, String jobTitle) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 22);
                content.setLeading(25f);
                content.newLineAtOffset(50, 750);

                content.showText("RESUDEX - Candidate Strategic Report");
                content.newLine();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.showText("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                content.newLine();
                content.newLine();

                content.setFont(PDType1Font.HELVETICA_BOLD, 16);
                content.showText("Candidate: " + candidateName);
                content.newLine();
                content.showText("Position: " + jobTitle);
                content.newLine();
                content.newLine();

                content.setFont(PDType1Font.HELVETICA_BOLD, 14);
                content.showText("MATCH SCORE: " + score.get_sc() + "%");
                content.newLine();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.showText("Experience detected: " + score.get_exp() + " years");
                content.newLine();
                content.newLine();

                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                content.showText("DETECTED SKILLS:");
                content.newLine();
                content.setFont(PDType1Font.HELVETICA, 11);
                String skills = String.join(", ", score.get_hits());
                if (skills.length() > 80) skills = skills.substring(0, 77) + "...";
                content.showText(skills);
                content.newLine();
                content.newLine();

                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                content.showText("AI INSIGHTS:");
                content.newLine();
                content.setFont(PDType1Font.HELVETICA, 11);
                for (String insight : score.get_recs()) {
                    if (insight.length() > 90) insight = insight.substring(0, 87) + "...";
                    content.showText("- " + insight);
                    content.newLine();
                }
                content.newLine();

                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                content.showText("DEVELOPMENT ROADMAP:");
                content.newLine();
                content.setFont(PDType1Font.HELVETICA, 11);
                for (String step : score.get_roadmap()) {
                    if (step.length() > 90) step = step.substring(0, 87) + "...";
                    content.showText(">> " + step);
                    content.newLine();
                }

                content.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}