package com.resudex.util;

import com.resudex.model.CompletenessResult;

import java.util.HashSet;
import java.util.Set;

public class ResumeCompletenessAnalyzer {

    public static CompletenessResult analyze(String resumeText) {

        Set<String> missing = new HashSet<>();
        int score = 0;

        if (contains(resumeText, "skill")) {
            score += 25;
        } else {
            missing.add("Skills");
        }

        if (contains(resumeText, "project")) {
            score += 25;
        } else {
            missing.add("Projects");
        }

        if (contains(resumeText, "experience")) {
            score += 25;
        } else {
            missing.add("Experience");
        }

        if (contains(resumeText, "education")) {
            score += 25;
        } else {
            missing.add("Education");
        }

        return new CompletenessResult(score, missing);
    }

    private static boolean contains(String text, String keyword) {
        return text.toLowerCase().contains(keyword);
    }
}