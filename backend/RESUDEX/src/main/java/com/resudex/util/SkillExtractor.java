package com.resudex.util;

import java.util.HashSet;
import java.util.Set;

public class SkillExtractor {

    public static Set<String> extractSkills(String text) {

        Set<String> foundSkills = new HashSet<>();
        String lower = text.toLowerCase();

        for (String raw : lower.split("\\W+")) {
            String normalized = SkillDictionary.normalize(raw);
            if (SkillRegistry.SKILLS.containsKey(normalized)) {
                foundSkills.add(normalized);
            }
        }

        for (String phrase : SkillRegistry.SKILLS.keySet()) {
            if (lower.contains(phrase)) {
                foundSkills.add(phrase);
            }
        }

        return foundSkills;
    }
}