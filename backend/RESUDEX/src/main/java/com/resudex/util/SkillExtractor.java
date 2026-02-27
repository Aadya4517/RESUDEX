package com.resudex.util;

import java.util.HashSet;
import java.util.Set;

public class SkillExtractor {

    public static Set<String> extractSkills(String text) {
        Set<String> foundSkills = new HashSet<>();
        String lowerText = text.toLowerCase();

        for (String skill : SkillDictionary.SKILLS) {
            if (lowerText.contains(skill)) {
                foundSkills.add(skill);
            }
        }
        return foundSkills;
    }
}