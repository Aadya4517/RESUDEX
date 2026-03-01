package com.resudex.util;

import com.resudex.model.ResumeScore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RankSummaryGenerator {

    public static List<String> generate(ResumeScore score) {

        List<String> summary = new ArrayList<>();

        summary.add("Matched skills: " + score.getMatchedSkills().size());

        if (score.getMissingMandatorySkills().isEmpty()) {
            summary.add("All mandatory skills satisfied");
        } else {
            summary.add("Missing mandatory skills: "
                    + score.getMissingMandatorySkills().size());
        }

        summary.add("Experience: "
                + score.getExperienceYears() + " years");

        summary.add("Resume completeness: "
                + score.getCompletenessScore() + "%");

        Map<String, Integer> roleFit = score.getRoleFit();

        String bestRole = null;
        int bestScore = 0;

        for (String role : roleFit.keySet()) {
            if (roleFit.get(role) > bestScore) {
                bestScore = roleFit.get(role);
                bestRole = role;
            }
        }

        if (bestRole != null) {
            summary.add("Best suited for: "
                    + bestRole + " (" + bestScore + "%)");
        }

        return summary;
    }
}