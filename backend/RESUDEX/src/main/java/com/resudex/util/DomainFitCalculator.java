package com.resudex.util;

import com.resudex.model.SkillCategory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DomainFitCalculator {

    public static Map<String, Integer> calculateDomainFit(Set<String> resumeSkills) {

        Map<SkillCategory, Integer> totalPerDomain = new HashMap<>();
        Map<SkillCategory, Integer> matchedPerDomain = new HashMap<>();

        for (String skill : SkillRegistry.SKILLS.keySet()) {
            SkillCategory category = SkillRegistry.getCategory(skill);

            totalPerDomain.put(
                    category,
                    totalPerDomain.getOrDefault(category, 0) + 1
            );

            if (resumeSkills.contains(skill)) {
                matchedPerDomain.put(
                        category,
                        matchedPerDomain.getOrDefault(category, 0) + 1
                );
            }
        }

        Map<String, Integer> result = new HashMap<>();

        for (SkillCategory category : totalPerDomain.keySet()) {

            int total = totalPerDomain.get(category);
            int matched = matchedPerDomain.getOrDefault(category, 0);

            int percent = (int) ((matched * 100.0) / total);

            result.put(category.name(), percent);
        }

        return result;
    }
}