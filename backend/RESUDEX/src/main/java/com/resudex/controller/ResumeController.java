package com.resudex.controller;

import com.resudex.model.ResumeScore;
import com.resudex.model.ResumeScorer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
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
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);

            ResumeScore score = scorer.scoreResume(
                    file.getOriginalFilename(),
                    text,
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

    @GetMapping("/health")
    public String health() {
        return "Resudex backend is running";
    }
}