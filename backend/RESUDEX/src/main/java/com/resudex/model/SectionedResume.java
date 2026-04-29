package com.resudex.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// splits resume into sections
public class SectionedResume {

    private final String raw;
    private final Map<String, String> sections = new HashMap<>();

    // section markers
    private static final Map<String, Pattern> MARKERS = Map.of(
            "EXPERIENCE", Pattern.compile("(?i)(experience|work history|employment|professional background)"),
            "PROJECTS",   Pattern.compile("(?i)(projects|academic projects|personal projects|technical projects)"),
            "SKILLS",     Pattern.compile("(?i)(skills|technical skills|tools|competencies|expertise)"),
            "EDUCATION",  Pattern.compile("(?i)(education|academic background|qualification)")
    );

    public SectionedResume(String text) {
        this.raw = text;
        parse();
    }

    private void parse() {
        String low = raw.toLowerCase();

        // find section start indices
        Map<String, Integer> idx = new HashMap<>();
        for (Map.Entry<String, Pattern> e : MARKERS.entrySet()) {
            Matcher m = e.getValue().matcher(low);
            if (m.find()) {
                idx.put(e.getKey(), m.start());
            }
        }

        // sort by position
        java.util.List<Map.Entry<String, Integer>> sorted = new java.util.ArrayList<>(idx.entrySet());
        sorted.sort(Map.Entry.comparingByValue());

        for (int i = 0; i < sorted.size(); i++) {
            String sec = sorted.get(i).getKey();
            int start = sorted.get(i).getValue();
            int end = (i + 1 < sorted.size()) ? sorted.get(i + 1).getValue() : raw.length();
            sections.put(sec, raw.substring(start, end).trim());
        }

        // header before first section
        if (!sorted.isEmpty() && sorted.get(0).getValue() > 0) {
            sections.put("HEADER", raw.substring(0, sorted.get(0).getValue()).trim());
        } else if (sorted.isEmpty()) {
            sections.put("HEADER", raw.trim());
        }
    }

    public String getSection(String name) {
        return sections.getOrDefault(name, "");
    }

    public boolean hasSkill(String sec, String skill) {
        String content = sections.get(sec);
        if (content == null) return false;
        return content.toLowerCase().contains(skill.toLowerCase());
    }

    public String getRawText() {
        return raw;
    }
}
