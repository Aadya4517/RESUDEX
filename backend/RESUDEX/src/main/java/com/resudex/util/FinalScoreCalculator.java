package com.resudex.util;

import java.util.Set;

public class FinalScoreCalculator {

    public static int calculateFinalScore(
            int skillScore,
            int experienceYears,
            Set<String> mandatorySkills,
            Set<String> resumeSkills
    ) {

        int experienceScore = Math.min(experienceYears * 10, 30);

        int penalty = 0;
        for (String skill : mandatorySkills) {
            if (!resumeSkills.contains(skill)) {
                penalty += 10;
            }
        }

        int finalScore = skillScore + experienceScore - penalty;

        return Math.max(finalScore, 0);
    }
}