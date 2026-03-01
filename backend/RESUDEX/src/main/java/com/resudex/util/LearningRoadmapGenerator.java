package com.resudex.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LearningRoadmapGenerator {

    public static List<String> generate(Set<String> high, Set<String> medium) {

        List<String> roadmap = new ArrayList<>();
        int step = 1;

        for (String skill : high) {
            roadmap.add("Step " + step++ + ": Learn " + skill + " fundamentals");
        }

        for (String skill : medium) {
            roadmap.add("Step " + step++ + ": Practice " + skill + " with a small project");
        }

        return roadmap;
    }
}