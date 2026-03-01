package com.resudex.util;

import java.util.Set;

public class ExplanationGenerator {

    public static String generate(
            Set<String> matched,
            Set<String> missingMandatory,
            int experience
    ) {

        StringBuilder sb = new StringBuilder();

        if (!matched.isEmpty()) {
            sb.append("Matched skills: ").append(String.join(", ", matched)).append(". ");
        }

        if (!missingMandatory.isEmpty()) {
            sb.append("Missing mandatory skills: ")
              .append(String.join(", ", missingMandatory)).append(". ");
        }

        if (experience == 0) {
            sb.append("No relevant experience detected.");
        } else {
            sb.append("Relevant experience: ").append(experience).append(" years.");
        }

        return sb.toString();
    }
}