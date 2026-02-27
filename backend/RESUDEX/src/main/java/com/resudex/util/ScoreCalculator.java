package com.resudex.util;

import java.util.Set;

public class ScoreCalculator {

    public static int calculateSkillScore(Set<String> resumeSkills, Set<String> jobSkills) {
        if (jobSkills.isEmpty()) return 0;

        int matched = 0;
        for (String skill : jobSkills) {
            if (resumeSkills.contains(skill)) {
                matched++;
            }
        }
        return (matched * 100) / jobSkills.size();
    }
}