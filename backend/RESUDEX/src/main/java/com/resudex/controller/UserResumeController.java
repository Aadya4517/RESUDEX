package com.resudex.controller;

import com.resudex.service.DatabaseService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * UserResumeController - lets a logged-in user upload their resume (PDF or DOCX).
 */
@RestController
@RequestMapping("/api/resume")
@CrossOrigin
public class UserResumeController {

    @Autowired
    private DatabaseService db;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadResume(
            @RequestParam("userId") int userId,
            @RequestParam("file")   MultipartFile file
    ) throws Exception {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file received"));
        }

        String filename = file.getOriginalFilename();
        String text     = extractText(file);

        if (text.isBlank()) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Could not extract text from the file. Make sure it is a valid PDF or DOCX.")
            );
        }

        db.saveResume(userId, filename, text);
        return ResponseEntity.ok(Map.of("message", "Resume uploaded successfully!", "filename", filename));
    }

    @GetMapping(value = "/export/{userId}", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportResumePdf(@PathVariable int userId) {
        Map<String, Object> user = db.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        String fullName = (String) user.getOrDefault("full_name", user.get("username"));
        String email = (String) user.get("email");
        String bio = (String) user.get("bio");
        String resumeText = "";
        
        Object rtObj = user.get("resume_text");
        if (rtObj != null) {
            if (rtObj instanceof String) resumeText = (String) rtObj;
            else if (rtObj instanceof java.sql.Clob) {
                try {
                    java.sql.Clob clob = (java.sql.Clob) rtObj;
                    resumeText = clob.getSubString(1, (int) clob.length());
                } catch (Exception ignored) {}
            } else {
                resumeText = rtObj.toString();
            }
        }

        try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
             PDDocument document = new PDDocument()) {

            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            document.addPage(page);

            try (org.apache.pdfbox.pdmodel.PDPageContentStream contentStream = 
                    new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page)) {
                
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 22);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText((fullName != null ? fullName : "ATS Optimized Resume").replaceAll("[^\\x00-\\x7F]", ""));
                contentStream.endText();

                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 680);
                contentStream.showText((email != null ? email : "").replaceAll("[^\\x00-\\x7F]", ""));
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 650);
                contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 10);
                contentStream.setLeading(14.5f);
                
                if (bio != null && !bio.isBlank()) {
                    contentStream.showText("Bio: " + bio.replaceAll("[^\\x00-\\x7F]", ""));
                    contentStream.newLine();
                    contentStream.newLine();
                }

                String[] lines = resumeText.split("\n");
                int lineCount = 0;
                for (String line : lines) {
                    if (line.isBlank()) continue;
                    String safeLine = line.replaceAll("[^\\x00-\\x7F]", " ").replace('\r', ' ').replace('\t', ' ');
                    safeLine = safeLine.length() > 90 ? safeLine.substring(0, 90) + "..." : safeLine;
                    contentStream.showText(safeLine);
                    contentStream.newLine();
                    lineCount++;
                    if (lineCount > 35) {
                        contentStream.showText("... [Content Truncated For ATS Demo]");
                        break;
                    }
                }
                contentStream.endText();
            }
            
            document.save(out);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
                    .body(out.toByteArray());
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // -------- Text extraction (same logic as existing ResumeController) --------
    private String extractText(MultipartFile file) throws Exception {
        String name = file.getOriginalFilename().toLowerCase();

        if (name.endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(file.getInputStream())) {
                return new PDFTextStripper().getText(doc);
            }
        }

        if (name.endsWith(".docx")) {
            try (InputStream is = file.getInputStream();
                 XWPFDocument doc = new XWPFDocument(is)) {
                StringBuilder sb = new StringBuilder();
                doc.getParagraphs().forEach(p -> sb.append(p.getText()).append(" "));
                return sb.toString();
            }
        }

        return "";
    }
}
