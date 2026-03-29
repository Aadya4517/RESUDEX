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
