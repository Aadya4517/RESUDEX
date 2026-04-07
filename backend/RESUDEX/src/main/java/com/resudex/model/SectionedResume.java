package com.resudex.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SectionedResume - Splits raw resume text into logical blocks (Experience, Projects, Education, etc.)
 * for more granular analysis and weighted scoring.
 */
public class SectionedResume {

    private final String rawText;
    private final Map<String, String> sections = new HashMap<>();

    // Section markers (common headers)
    private static final Map<String, Pattern> MARKERS = Map.of(
            "EXPERIENCE", Pattern.compile("(?i)(experience|work history|employment|professional background)"),
            "PROJECTS",   Pattern.compile("(?i)(projects|academic projects|personal projects|technical projects)"),
            "SKILLS",     Pattern.compile("(?i)(skills|technical skills|tools|competencies|expertise)"),
            "EDUCATION",  Pattern.compile("(?i)(education|academic background|qualification)")
    );

    public SectionedResume(String text) {
        this.rawText = text;
        parse();
    }

    private void parse() {
        String lower = rawText.toLowerCase();
        
        // Find best-guess start indices for each section
        Map<String, Integer> startIndices = new HashMap<>();
        for (Map.Entry<String, Pattern> entry : MARKERS.entrySet()) {
            Matcher m = entry.getValue().matcher(lower);
            if (m.find()) {
                startIndices.put(entry.getKey(), m.start());
            }
        }

        // Sort indices to determine section bounds
        java.util.List<Map.Entry<String, Integer>> sorted = new java.util.ArrayList<>(startIndices.entrySet());
        sorted.sort(Map.Entry.comparingByValue());

        for (int i = 0; i < sorted.size(); i++) {
            String currentSection = sorted.get(i).getKey();
            int start = sorted.get(i).getValue();
            int end = (i + 1 < sorted.size()) ? sorted.get(i + 1).getValue() : rawText.length();
            
            sections.put(currentSection, rawText.substring(start, end).trim());
        }

        // Anything before the first section is likely "Summary" or "Header"
        if (!sorted.isEmpty() && sorted.get(0).getValue() > 0) {
            sections.put("HEADER", rawText.substring(0, sorted.get(0).getValue()).trim());
        } else if (sorted.isEmpty()) {
            sections.put("HEADER", rawText.trim()); // Fallback
        }
    }

    public String getSection(String name) {
        return sections.getOrDefault(name, "");
    }

    public boolean hasSkill(String sectionName, String skill) {
        String content = sections.get(sectionName);
        if (content == null) return false;
        return content.toLowerCase().contains(skill.toLowerCase());
    }

    public String getRawText() {
        return rawText;
    }
}
