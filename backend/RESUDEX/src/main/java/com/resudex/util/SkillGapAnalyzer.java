package com.resudex.util;

import com.resudex.model.SkillGapPriority;
import java.util.Set;

public class SkillGapAnalyzer {

    public static SkillGapPriority analyze(Set<String> missingMandatorySkills) {

        if (missingMandatorySkills.size() > 5) {
            return SkillGapPriority.HIGH;
        } else if (missingMandatorySkills.size() > 2) {
            return SkillGapPriority.MEDIUM;
        } else {
            return SkillGapPriority.LOW;
        }
    }
}