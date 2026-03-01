package com.resudex.model;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.resudex.model.SkillGapPriority;

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

        Map<String, Integer> domainFit = new HashMap<>();
        Map<String, Integer> roleFit = new HashMap<>();
        List<String> rankSummary = new ArrayList<>();

        int finalScore = 0;

        for (Map.Entry<String, Set<String>> entry : DOMAIN_SKILLS.entrySet()) {
            String domain = entry.getKey();
            Set<String> skills = entry.getValue();

            Set<String> matched = skills.stream()
                    .filter(resumeText::contains)
                    .collect(Collectors.toSet());

            matchedSkills.addAll(matched);

            int fitPercent = (int) ((matched.size() * 100.0) / skills.size());
            domainFit.put(domain, fitPercent);
        }

        for (String skill : mandatorySkills) {
            if (!resumeText.contains(skill)) {
                missingMandatorySkills.add(skill);
            }
        }

        Set<String> jdSkills = extractMandatorySkills(jobDescription);

for (String skill : jdSkills) {
    if (resumeText.contains(skill)) {
        matchedSkills.add(skill);
    } else {
        missingSkills.add(skill);
    }
}

        finalScore += Math.min(40, matchedSkills.size() * 3);

        if (experienceYears >= 3) finalScore += 20;
        else if (experienceYears >= 1) finalScore += 10;

        finalScore -= missingMandatorySkills.size() * 5;

        finalScore = Math.max(0, Math.min(finalScore, 100));

        if (experienceYears > 0)
            rankSummary.add("Has " + experienceYears + " years of experience");

        if (!matchedSkills.isEmpty())
            rankSummary.add("Matched " + matchedSkills.size() + " relevant skills");

        if (!missingMandatorySkills.isEmpty())
            rankSummary.add("Missing mandatory skills: " + missingMandatorySkills.size());

        SkillGapPriority priority;
        if (missingMandatorySkills.size() > 5) priority = SkillGapPriority.HIGH;
        else if (missingMandatorySkills.size() > 2) priority = SkillGapPriority.MEDIUM;
        else priority = SkillGapPriority.LOW;

        List<String> learningRoadmap = missingSkills.stream()
                .limit(5)
                .map(skill -> "Learn " + skill)
                .toList();

        return new ResumeScore(
                fileName,
                finalScore,
                experienceYears,
                matchedSkills,
                missingSkills,
                missingMandatorySkills,
                "Score calculated using skills, experience and job relevance",
                100,
                Set.of(),
                priority,
                learningRoadmap,
                roleFit,
                domainFit,
                rankSummary
        );
    }

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