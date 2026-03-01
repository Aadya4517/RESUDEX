package com.resudex.model;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ResumeScorer {

    private static final Map<String, Set<String>> DOMAIN_SKILLS = Map.of(
            "Java Backend", Set.of(
                    "java","spring","spring boot","hibernate","jpa",
                    "jdbc","maven","gradle","rest","microservices"
            ),
            "Web Development", Set.of(
                    "html","css","javascript","react","angular",
                    "vue","bootstrap","tailwind","node"
            ),
            "Python", Set.of(
                    "python","django","flask","fastapi",
                    "numpy","pandas","scikit-learn"
            ),
            "C / C++", Set.of(
                    "c","c++","stl","pointers",
                    "data structures","memory management"
            ),
            "DevOps", Set.of(
                    "docker","kubernetes","aws","azure",
                    "gcp","ci/cd","jenkins","linux"
            ),
            "Databases", Set.of(
                    "mysql","postgresql","mongodb","redis","sql"
            ),
            "Mobile Development", Set.of(
                    "android","kotlin","swift","flutter","react native"
            )
    );

    private static final Map<String, Set<String>> ROLE_SKILLS = Map.of(
            "Backend Developer", Set.of("java","spring","sql","rest","microservices"),
            "Full Stack Developer", Set.of("java","spring","react","javascript","sql"),
            "Data Engineer", Set.of("python","sql","pandas","spark"),
            "DevOps Engineer", Set.of("docker","kubernetes","aws","ci/cd")
    );

    public ResumeScore scoreResume(
            String fileName,
            String resumeText,
            String jobDescription
    ) {

        resumeText = resumeText.toLowerCase();
        jobDescription = jobDescription.toLowerCase();

        int experienceYears = extractExperienceYears(resumeText);

        Set<String> matchedSkills = new HashSet<>();
        Set<String> missingSkills = new HashSet<>();
        Set<String> mandatorySkills = extractMandatorySkills(jobDescription);
        Set<String> missingMandatorySkills = new HashSet<>();

        Map<String, Integer> domainFit = new LinkedHashMap<>();
        Map<String, Integer> roleFit = new LinkedHashMap<>();
        List<String> rankSummary = new ArrayList<>();

        /* ---------- DOMAIN FIT ---------- */
        for (Map.Entry<String, Set<String>> entry : DOMAIN_SKILLS.entrySet()) {
            String domain = entry.getKey();
            Set<String> skills = entry.getValue();

            long matched = skills.stream()
                    .filter(resumeText::contains)
                    .count();

            int fitPercent = (int) ((matched * 100.0) / skills.size());
            domainFit.put(domain, fitPercent);

            skills.stream()
                    .filter(resumeText::contains)
                    .forEach(matchedSkills::add);
        }

        /* ---------- ROLE FIT ---------- */
        for (Map.Entry<String, Set<String>> entry : ROLE_SKILLS.entrySet()) {
            String role = entry.getKey();
            Set<String> skills = entry.getValue();

            long matched = skills.stream()
                    .filter(resumeText::contains)
                    .count();

            int fitPercent = (int) ((matched * 100.0) / skills.size());
            roleFit.put(role, fitPercent);
        }

        /* ---------- SKILL GAP ---------- */
        for (String skill : mandatorySkills) {
            if (!resumeText.contains(skill)) {
                missingMandatorySkills.add(skill);
                missingSkills.add(skill);
            } else {
                matchedSkills.add(skill);
            }
        }

        /* ---------- SCORING (ONLY ONE LOGIC) ---------- */
        int skillScore = Math.min(50, matchedSkills.size() * 4);

        int experienceScore;
        if (experienceYears >= 5) experienceScore = 25;
        else if (experienceYears >= 3) experienceScore = 18;
        else if (experienceYears >= 1) experienceScore = 10;
        else experienceScore = 0;

        int domainScore = domainFit.values()
                .stream()
                .max(Integer::compareTo)
                .orElse(0) / 4;

        int penalty = missingMandatorySkills.size() * 8;

        int finalScore = skillScore + experienceScore + domainScore - penalty;
        finalScore = Math.max(0, Math.min(finalScore, 100));

        /* ---------- SUMMARY ---------- */
        rankSummary.add("Matched " + matchedSkills.size() + " relevant skills");
        rankSummary.add("Experience: " + experienceYears + " years");
        rankSummary.add("Missing mandatory skills: " + missingMandatorySkills.size());

        /* ---------- SKILL GAP PRIORITY ---------- */
        SkillGapPriority priority;
        if (missingMandatorySkills.size() >= 5) priority = SkillGapPriority.HIGH;
        else if (missingMandatorySkills.size() >= 3) priority = SkillGapPriority.MEDIUM;
        else priority = SkillGapPriority.LOW;

        /* ---------- LEARNING ROADMAP ---------- */
        List<String> learningRoadmap = missingMandatorySkills.stream()
                .limit(6)
                .map(skill -> "Mandatory: Learn " + skill)
                .collect(Collectors.toList());

        return new ResumeScore(
                fileName,
                finalScore,
                experienceYears,
                matchedSkills,
                missingSkills,
                missingMandatorySkills,
                "Score derived from skill match, experience, domain fit and penalties",
                100,
                Set.of(),
                priority,
                learningRoadmap,
                roleFit,
                domainFit,
                rankSummary
        );
    }

    /* ---------- EXPERIENCE EXTRACTION ---------- */
    private int extractExperienceYears(String text) {
        Pattern p = Pattern.compile("(\\d+)\\s*(years?|yrs?|months?)");
        Matcher m = p.matcher(text);

        int months = 0;
        while (m.find()) {
            int val = Integer.parseInt(m.group(1));
            String unit = m.group(2);

            if (unit.contains("year") || unit.contains("yr")) {
                months += val * 12;
            } else {
                months += val;
            }
        }
        return months / 12;
    }

    /* ---------- JD SKILL EXTRACTION ---------- */
    private Set<String> extractMandatorySkills(String jobDescription) {
        Set<String> mandatory = new HashSet<>();

        for (Set<String> skills : DOMAIN_SKILLS.values()) {
            for (String skill : skills) {
                if (jobDescription.contains(skill)) {
                    mandatory.add(skill);
                }
            }
        }
        return mandatory;
    }
}