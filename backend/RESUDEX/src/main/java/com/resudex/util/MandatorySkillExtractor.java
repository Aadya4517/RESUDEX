package com.resudex.util;

import java.util.Set;
import java.util.stream.Collectors;

public class MandatorySkillExtractor {

    // extract top mandatory skills
    public static Set<String> extractMandatorySkills(String jd) {
        return SkillExtractor.extractSkills(jd)
                .stream()
                .limit(3)
                .collect(Collectors.toSet());
    }
}
