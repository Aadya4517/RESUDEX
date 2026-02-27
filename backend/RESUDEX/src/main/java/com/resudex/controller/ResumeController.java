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
        Set<String> mandatorySkills = MandatorySkillExtractor.extractMandatorySkills(jobDescription);

        List<ResumeScore> results = new ArrayList<>();

        for (MultipartFile file : files) {

            String resumeText = ResumeTextExtractor.extractText(file);

            Set<String> resumeSkills = SkillExtractor.extractSkills(resumeText);

            int skillScore = ScoreCalculator.calculateSkillScore(resumeSkills, jobSkills);

            int experienceYears = ExperienceExtractor.extractYears(resumeText);

            Set<String> matchedSkills = new HashSet<>(resumeSkills);
            matchedSkills.retainAll(jobSkills);

            int finalScore = FinalScoreCalculator.calculateFinalScore(
                    skillScore,
                    experienceYears,
                    mandatorySkills,
                    resumeSkills
            );

            results.add(
                    new ResumeScore(
                            file.getOriginalFilename(),
                            finalScore,
                            experienceYears,
                            matchedSkills
                    )
            );
        }

        results.sort((a, b) -> Integer.compare(b.getFinalScore(), a.getFinalScore()));
        return results;
    }
}