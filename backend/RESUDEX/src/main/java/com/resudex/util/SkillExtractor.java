package com.resudex.util;

import java.util.HashSet;
import java.util.Set;

public class SkillExtractor {

    // extract skills from text
    public static Set<String> extractSkills(String txt) {
        Set<String> found = new HashSet<>();
        String low = txt.toLowerCase();

        for (String raw : low.split("\\W+")) {
            String norm = SkillDictionary.normalize(raw);
            if (SkillRegistry.SKILLS.containsKey(norm)) {
                found.add(norm);
            }
        }

        for (String phrase : SkillRegistry.SKILLS.keySet()) {
            if (low.contains(phrase)) {
                found.add(phrase);
            }
        }

        return found;
    }
}
