package com.resudex.controller;

import com.resudex.model.ResumeScore;
import com.resudex.model.ResumeScorer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ResumeController {

    private final ResumeScorer scorer = new ResumeScorer();

    @PostMapping("/uploadMultiple")
    public Map<String, Object> uploadMultiple(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("jobDescription") String jobDescription
    ) throws Exception {

        List<ResumeScore> scores = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String resumeText = extractTextFromFile(file);

            ResumeScore score = scorer.scoreResume(
                    file.getOriginalFilename(),
                    resumeText,
                    jobDescription
            );

            scores.add(score);
        }

        scores.sort((a, b) -> b.getFinalScore() - a.getFinalScore());

        double avg = scores.stream()
                .mapToInt(ResumeScore::getFinalScore)
                .average()
                .orElse(0);

        int highest = scores.isEmpty() ? 0 : scores.get(0).getFinalScore();

        Map<String, Object> response = new HashMap<>();
        response.put("totalResumes", scores.size());
        response.put("averageScore", avg);
        response.put("highestScore", highest);
        response.put("rankedResumes", scores);

        return response;
    }

    private String extractTextFromFile(MultipartFile file) throws Exception {

        String fileName = file.getOriginalFilename().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                return new PDFTextStripper().getText(document);
            }
        }

        if (fileName.endsWith(".docx")) {
            try (InputStream is = file.getInputStream();
                 XWPFDocument doc = new XWPFDocument(is)) {
                StringBuilder text = new StringBuilder();
                doc.getParagraphs().forEach(p -> text.append(p.getText()).append(" "));
                return text.toString();
            }
        }

        return "";
    }

    @GetMapping("/health")
    public String health() {
        return "Resudex backend is running";
    }
}