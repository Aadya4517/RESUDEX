package com.resudex.util;

import com.resudex.model.JDAnalysis;

import java.util.*;
import java.util.regex.*;

public class JDAnalyzer {

    private static final Set<String> SOFT_SKILLS = Set.of(
            "communication", "leadership", "teamwork",
            "collaboration", "problem solving", "ownership"
    );

    public static JDAnalysis analyze(String jobDescription) {

        Set<String> skills = SkillExtractor.extractSkills(jobDescription);
        Set<String> mandatory = MandatorySkillExtractor.extractMandatorySkills(jobDescription);

        Set<String> optional = new HashSet<>(skills);
        optional.removeAll(mandatory);

        int experience = extractExperience(jobDescription);
        Set<String> softSkills = extractSoftSkills(jobDescription);

        return new JDAnalysis(
                skills.size(),
                mandatory,
                optional,
                experience,
                softSkills
        );
    }

    private static int extractExperience(String text) {
        Pattern p = Pattern.compile("(\\d+)\\+?\\s*(years|year)");
        Matcher m = p.matcher(text.toLowerCase());
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    private static Set<String> extractSoftSkills(String text) {
        Set<String> found = new HashSet<>();
        String lower = text.toLowerCase();
        for (String skill : SOFT_SKILLS) {
            if (lower.contains(skill)) {
                found.add(skill);
            }
        }
        return found;
    }
}