package com.resudex.util;

import java.util.Set;

public class FinalScoreCalculator {

    public static int calculateFinalScore(
            Set<String> resumeSkills,
            Set<String> jobSkills,
            Set<String> mandatorySkills,
            int experienceYears
    ) {

        int skillMatch = calculateSkillMatch(resumeSkills, jobSkills);
        int experienceScore = Math.min(experienceYears * 10, 30);
        int mandatoryPenalty = calculateMandatoryPenalty(resumeSkills, mandatorySkills);

        int finalScore = skillMatch + experienceScore - mandatoryPenalty;

        return Math.max(0, Math.min(finalScore, 100));
    }

    private static int calculateSkillMatch(Set<String> resumeSkills, Set<String> jobSkills) {
        if (jobSkills.isEmpty()) return 0;
        long matched = resumeSkills.stream().filter(jobSkills::contains).count();
        return (int) ((matched * 60.0) / jobSkills.size());
    }

    private static int calculateMandatoryPenalty(Set<String> resumeSkills, Set<String> mandatorySkills) {
        long missingMandatory = mandatorySkills.stream()
                .filter(skill -> !resumeSkills.contains(skill))
                .count();

        return (int) (missingMandatory * 5);
    }
}