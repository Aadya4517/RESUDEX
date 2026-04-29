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

    // extract text from pdf
    public static String extractText(InputStream in) {
        try {
            PDDocument doc = PDDocument.load(in);
            PDFTextStripper sc = new PDFTextStripper();
            String txt = sc.getText(doc);
            doc.close();
            return txt;
        } catch (Exception e) {
            return "";
        }
    }

    // generate report pdf
    public static byte[] generateShortlistReport(ResumeScore res, String name, String jobTitle) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 22);
                cs.setLeading(25f);
                cs.newLineAtOffset(50, 750);

                cs.showText("RESUDEX - Candidate Strategic Report");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.showText("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                cs.newLine();
                cs.newLine();

                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs.showText("Candidate: " + name);
                cs.newLine();
                cs.showText("Position: " + jobTitle);
                cs.newLine();
                cs.newLine();

                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.showText("MATCH SCORE: " + res.get_sc() + "%");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.showText("Experience detected: " + res.get_exp() + " years");
                cs.newLine();
                cs.newLine();

                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.showText("DETECTED SKILLS:");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 11);
                String skills = String.join(", ", res.get_hits());
                if (skills.length() > 80) skills = skills.substring(0, 77) + "...";
                cs.showText(skills);
                cs.newLine();
                cs.newLine();

                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.showText("AI INSIGHTS:");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 11);
                for (String insight : res.get_recs()) {
                    if (insight.length() > 90) insight = insight.substring(0, 87) + "...";
                    cs.showText("- " + insight);
                    cs.newLine();
                }
                cs.newLine();

                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.showText("DEVELOPMENT ROADMAP:");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 11);
                for (String step : res.get_roadmap()) {
                    if (step.length() > 90) step = step.substring(0, 87) + "...";
                    cs.showText(">> " + step);
                    cs.newLine();
                }

                cs.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
