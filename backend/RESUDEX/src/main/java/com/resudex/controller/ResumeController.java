package com.resudex.controller;

import com.resudex.model.ResumeScore;
import com.resudex.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ResumeController {

    @PostMapping("/uploadMultiple")
    public List<ResumeScore> uploadMultipleResumes(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("jobDescription") String jobDescription
    ) {

        Set<String> jobSkills = SkillExtractor.extractSkills(jobDescription);

        List<ResumeScore> results = new ArrayList<>();

        for (MultipartFile file : files) {

            String resumeText = ResumeTextExtractor.extractText(file);

            Set<String> resumeSkills = SkillExtractor.extractSkills(resumeText);

            int score = ScoreCalculator.calculateSkillScore(resumeSkills, jobSkills);

            Set<String> matched = new HashSet<>(resumeSkills);
            matched.retainAll(jobSkills);

            results.add(new ResumeScore(file.getOriginalFilename(), score, matched));
        }

        results.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        return results;
    }
}