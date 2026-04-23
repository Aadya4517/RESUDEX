package com.resudex.controller;

import com.resudex.model.ResumeScore;
import com.resudex.model.ResumeScorer;
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
 * Controller for users to upload their CVs.
 */
@RestController
@RequestMapping("/api/resume")
@CrossOrigin
public class UserResumeController {

    @Autowired
    private DatabaseService app_db;

    private final ResumeScorer snap_scorer = new ResumeScorer();

    @PostMapping("/push_cv")
    public ResponseEntity<Map<String, Object>> submit_cv(
            @RequestParam("userId") int uid,
            @RequestParam("file")   MultipartFile f
    ) throws Exception {

        if (f.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file found in request"));
        }

        String fname = f.getOriginalFilename();
        if (fname == null) fname = "resume.pdf";

        String raw_txt = parse_file(f);

        // Log for debugging
        System.out.println("[CV UPLOAD] uid=" + uid + " file=" + fname + " extracted_length=" + raw_txt.length());

        if (raw_txt.isBlank()) {
            System.out.println("[CV UPLOAD] Extraction returned blank for file: " + fname);
            return ResponseEntity.badRequest().body(
                Map.of("error", "Could not extract text from your PDF. Make sure it is a text-based PDF (not scanned/image). Try saving it as PDF from Word or Google Docs.")
            );
        }

        app_db.store_cv_text(uid, fname, raw_txt);

        // save trajectory snapshot
        try {
            ResumeScore snap = snap_scorer.scan(fname, raw_txt, "");
            Map<String, Integer> doms = snap.get_domains();
            app_db.save_snapshot(uid, fname,
                doms.getOrDefault("Java Backend",    0),
                doms.getOrDefault("Web Development", 0),
                doms.getOrDefault("Python",          0),
                doms.getOrDefault("C / C++",         0),
                doms.getOrDefault("DevOps",          0),
                doms.getOrDefault("Databases",       0),
                snap.get_exp()
            );
        } catch (Exception ignored) {}

        System.out.println("[CV UPLOAD] Saved successfully for uid=" + uid);
        return ResponseEntity.ok(Map.of("message", "CV saved successfully!", "filename", fname));
    }

    @GetMapping(value = "/get_pdf/{uid}", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> get_cv_pdf(@PathVariable int uid) {
        Map<String, Object> u = app_db.get_usr_by_id(uid);
        if (u == null) {
            return ResponseEntity.notFound().build();
        }
        
        String u_name = (String) u.getOrDefault("full_name", u.get("username"));
        String u_mail = (String) u.get("email");
        String u_bio = (String) u.get("bio");
        String cv_text = "";
        
        Object rt = u.get("resume_text");
        if (rt != null) {
            if (rt instanceof String) cv_text = (String) rt;
            else if (rt instanceof java.sql.Clob) {
                try {
                    java.sql.Clob clob = (java.sql.Clob) rt;
                    cv_text = clob.getSubString(1, (int) clob.length());
                } catch (Exception ignored) {}
            } else {
                cv_text = rt.toString();
            }
        }

        try (java.io.ByteArrayOutputStream out_stream = new java.io.ByteArrayOutputStream();
             PDDocument doc = new PDDocument()) {

            org.apache.pdfbox.pdmodel.PDPage p = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(p);

            try (org.apache.pdfbox.pdmodel.PDPageContentStream stream = 
                    new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, p)) {
                
                stream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 22);
                stream.beginText();
                stream.newLineAtOffset(50, 700);
                stream.showText((u_name != null ? u_name : "ATS Resume").replaceAll("[^\\x00-\\x7F]", ""));
                stream.endText();

                stream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12);
                stream.beginText();
                stream.newLineAtOffset(50, 680);
                stream.showText((u_mail != null ? u_mail : "").replaceAll("[^\\x00-\\x7F]", ""));
                stream.endText();

                stream.beginText();
                stream.newLineAtOffset(50, 650);
                stream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 10);
                stream.setLeading(14.5f);
                
                if (u_bio != null && !u_bio.isBlank()) {
                    stream.showText("Bio: " + u_bio.replaceAll("[^\\x00-\\x7F]", ""));
                    stream.newLine();
                    stream.newLine();
                }

                String[] lines = cv_text.split("\n");
                int count = 0;
                for (String l : lines) {
                    if (l.isBlank()) continue;
                    String clean = l.replaceAll("[^\\x00-\\x7F]", " ").replace('\r', ' ').replace('\t', ' ');
                    clean = clean.length() > 90 ? clean.substring(0, 90) + "..." : clean;
                    stream.showText(clean);
                    stream.newLine();
                    count++;
                    if (count > 35) {
                        stream.showText("... [Content Truncated For ATS Demo]");
                        break;
                    }
                }
                stream.endText();
            }
            
            doc.save(out_stream);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
                    .body(out_stream.toByteArray());
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String parse_file(MultipartFile f) throws Exception {
        String n = f.getOriginalFilename().toLowerCase();

        if (n.endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(f.getInputStream())) {
                return new PDFTextStripper().getText(doc);
            }
        }

        if (n.endsWith(".docx")) {
            try (InputStream s = f.getInputStream();
                 XWPFDocument d = new XWPFDocument(s)) {
                StringBuilder b = new StringBuilder();
                d.getParagraphs().forEach(para -> b.append(para.getText()).append(" "));
                return b.toString();
            }
        }

        return "";
    }
}
