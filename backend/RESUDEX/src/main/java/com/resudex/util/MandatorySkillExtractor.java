package com.resudex.util;

import java.util.Set;
import java.util.stream.Collectors;

public class MandatorySkillExtractor {

    public static Set<String> extractMandatorySkills(String jobDescription) {
        return SkillExtractor.extractSkills(jobDescription)
                .stream()
                .limit(3)
                .collect(Collectors.toSet());
    }
}