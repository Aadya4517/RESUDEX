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

// cv upload endpoints
@RestController
@RequestMapping("/api/resume")
@CrossOrigin
public class UserResumeController {

    @Autowired
    private DatabaseService db;

    private final ResumeScorer scorer = new ResumeScorer();

    // upload cv
    @PostMapping("/push_cv")
    public ResponseEntity<Map<String, Object>> uploadCV(
            @RequestParam("userId") int uid,
            @RequestParam("file")   MultipartFile f
    ) throws Exception {

        if (f.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file found in request"));
        }

        String fn = f.getOriginalFilename();
        if (fn == null) fn = "resume.pdf";

        String txt = parseFile(f);

        System.out.println("[CV UPLOAD] uid=" + uid + " file=" + fn + " extracted_length=" + txt.length());

        if (txt.isBlank()) {
            System.out.println("[CV UPLOAD] Extraction returned blank for file: " + fn);
            return ResponseEntity.badRequest().body(
                Map.of("error", "Could not extract text from your PDF. Make sure it is a text-based PDF (not scanned/image). Try saving it as PDF from Word or Google Docs.")
            );
        }

        db.saveResume(uid, fn, txt);

        // save trajectory snapshot
        try {
            ResumeScore snap = scorer.scan(fn, txt, "");
            Map<String, Integer> doms = snap.get_domains();
            db.saveSnapshot(uid, fn,
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
        return ResponseEntity.ok(Map.of("message", "CV saved successfully!", "filename", fn));
    }

    // get cv as pdf
    @GetMapping(value = "/get_pdf/{uid}", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getCvPdf(@PathVariable int uid) {
        Map<String, Object> u = db.getUser(uid);
        if (u == null) {
            return ResponseEntity.notFound().build();
        }

        String uName = (String) u.getOrDefault("full_name", u.get("username"));
        String uMail = (String) u.get("email");
        String uBio  = (String) u.get("bio");
        String cvTxt = "";

        Object rt = u.get("resume_text");
        if (rt != null) {
            if (rt instanceof String) cvTxt = (String) rt;
            else if (rt instanceof java.sql.Clob) {
                try {
                    java.sql.Clob clob = (java.sql.Clob) rt;
                    cvTxt = clob.getSubString(1, (int) clob.length());
                } catch (Exception ignored) {}
            } else {
                cvTxt = rt.toString();
            }
        }

        try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
             PDDocument doc = new PDDocument()) {

            org.apache.pdfbox.pdmodel.PDPage p = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(p);

            try (org.apache.pdfbox.pdmodel.PDPageContentStream cs =
                    new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, p)) {

                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 22);
                cs.beginText();
                cs.newLineAtOffset(50, 700);
                cs.showText((uName != null ? uName : "ATS Resume").replaceAll("[^\\x00-\\x7F]", ""));
                cs.endText();

                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12);
                cs.beginText();
                cs.newLineAtOffset(50, 680);
                cs.showText((uMail != null ? uMail : "").replaceAll("[^\\x00-\\x7F]", ""));
                cs.endText();

                cs.beginText();
                cs.newLineAtOffset(50, 650);
                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 10);
                cs.setLeading(14.5f);

                if (uBio != null && !uBio.isBlank()) {
                    cs.showText("Bio: " + uBio.replaceAll("[^\\x00-\\x7F]", ""));
                    cs.newLine();
                    cs.newLine();
                }

                String[] lines = cvTxt.split("\n");
                int count = 0;
                for (String l : lines) {
                    if (l.isBlank()) continue;
                    String clean = l.replaceAll("[^\\x00-\\x7F]", " ").replace('\r', ' ').replace('\t', ' ');
                    clean = clean.length() > 90 ? clean.substring(0, 90) + "..." : clean;
                    cs.showText(clean);
                    cs.newLine();
                    count++;
                    if (count > 35) {
                        cs.showText("... [Content Truncated For ATS Demo]");
                        break;
                    }
                }
                cs.endText();
            }

            doc.save(out);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
                    .body(out.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // parse pdf or docx
    private String parseFile(MultipartFile f) throws Exception {
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
