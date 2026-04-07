package com.resudex.model;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ResumeScorer {

    private static final Map<String, Set<String>> DOMAIN_SKILLS = Map.of(
            "Java Backend", Set.of(
                    "java","spring","spring boot","hibernate","jpa",
                    "jdbc","maven","gradle","rest","microservices","tomcat","jetty","servlets"
            ),
            "Web Development", Set.of(
                    "html","css","javascript","react","angular",
                    "vue","bootstrap","tailwind","node","typescript","express","sass","less","jquery","webpack"
            ),
            "Python", Set.of(
                    "python","django","flask","fastapi",
                    "numpy","pandas","scikit-learn","tensorflow","pytorch","keras","matplotlib"
            ),
            "C / C++", Set.of(
                    "c","c++","stl","pointers",
                    "data structures","memory management","cmake","make","embedded","firmware"
            ),
            "DevOps", Set.of(
                    "docker","kubernetes","aws","azure",
                    "gcp","ci/cd","jenkins","linux","terraform","ansible","prometheus","grafana"
            ),
            "Databases", Set.of(
                    "mysql","postgresql","mongodb","redis","sql","oracle","sqlite","cassandra","elasticsearch"
            ),
            "Cloud & Misc", Set.of(
                    "api","git","agile","jira","scrum","unit testing","junit","mockito","rest api","json","xml"
            ),
            "General Tech & Roles", Set.of(
                    "software","engineer","developer","programmer","coder","manager","lead","senior","junior","intern",
                    "architect","analyst","specialist","consultant","fullstack","frontend","backend","office","cloud"
            )
    );

    private static final Map<String, Set<String>> ROLE_SKILLS = Map.of(
            "Python Developer", Set.of("python","django","flask","fastapi","sql"),
            "Java Backend Engineer", Set.of("java","spring","sql","rest","microservices","hibernate"),
            "Full Stack Developer", Set.of("java","spring","react","javascript","sql","html","css"),
            "Data Engineer", Set.of("python","sql","pandas","spark","numpy"),
            "DevOps Engineer", Set.of("docker","kubernetes","aws","ci/cd","jenkins","linux")
    );

    public ResumeScore scoreResume(
            String fileName,
            String resumeText,
            String jobDescription
    ) {

        SectionedResume sr = new SectionedResume(resumeText);
        jobDescription = jobDescription.toLowerCase();

        int experienceYears = extractExperienceYears(resumeText);

        Set<String> matchedSkills = new HashSet<>();
        Set<String> missingSkills = new HashSet<>();
        Set<String> mandatorySkills = extractMandatorySkills(jobDescription);
        Set<String> missingMandatorySkills = new HashSet<>();

        Map<String, Integer> domainFit = new LinkedHashMap<>();
        Map<String, Integer> roleFit = new LinkedHashMap<>();
        List<String> rankSummary = new ArrayList<>();

        /* ---------- DEEP SKILL ANALYSIS (Weighted) ---------- */
        double weightedSkillMatchTotal = 0;
        for (String skill : mandatorySkills) {
            String lowerSkill = skill.toLowerCase();
            boolean found = false;
            double skillWeight = 0;

            // 1. Check Experience Section (Highest Weight: 2x)
            if (sr.hasSkill("EXPERIENCE", lowerSkill)) {
                skillWeight = 2.0;
                found = true;
            }
            // 2. Check Projects Section (High Weight: 1.5x)
            else if (sr.hasSkill("PROJECTS", lowerSkill)) {
                skillWeight = 1.5;
                found = true;
            }
            // 3. Check General Skills Section (Standard Weight: 1.0x)
            else if (sr.hasSkill("SKILLS", lowerSkill)) {
                skillWeight = 1.0;
                found = true;
            }
            // 4. Generic Check (Lowest Weight: 0.8x if matched but section unknown)
            else if (resumeText.toLowerCase().contains(lowerSkill)) {
                skillWeight = 0.8;
                found = true;
            }

            if (found) {
                matchedSkills.add(skill);
                weightedSkillMatchTotal += skillWeight;
            } else {
                missingMandatorySkills.add(skill);
                missingSkills.add(skill);
            }
        }

        /* ---------- DOMAIN FIT ---------- */
        for (Map.Entry<String, Set<String>> entry : DOMAIN_SKILLS.entrySet()) {
            String domain = entry.getKey();
            Set<String> skills = entry.getValue();

            long matched = skills.stream()
                    .filter(resumeText.toLowerCase()::contains)
                    .count();

            int fitPercent = (int) ((matched * 100.0) / skills.size());
            domainFit.put(domain, fitPercent);
        }

        /* ---------- ROLE FIT ---------- */
        for (Map.Entry<String, Set<String>> entry : ROLE_SKILLS.entrySet()) {
            String role = entry.getKey();
            Set<String> skills = entry.getValue();

            long matched = skills.stream()
                    .filter(resumeText.toLowerCase()::contains)
                    .count();

            int fitPercent = (int) ((matched * 100.0) / skills.size());
            roleFit.put(role, fitPercent);
        }

        /* ---------- SCORING (DEEP LOGIC) ---------- */
        // Skill Match Score (Max 70 - more emphasis)
        double skillMatchRatio = mandatorySkills.isEmpty() ? 0 : weightedSkillMatchTotal / (mandatorySkills.size() * 2.0); 
        int skillScore = (int) (skillMatchRatio * 70);

        // Experience Score (Max 25): dynamically based on JD range if found
        int experienceScore = 0;
        int[] jdRange = extractExperienceRangeFromJD(jobDescription);
        if (jdRange != null) {
            int min = jdRange[0];
            int max = jdRange[1];
            if (experienceYears >= min && experienceYears <= max) experienceScore = 25;
            else if (experienceYears >= min - 1 && experienceYears <= max + 1) experienceScore = 18;
            else if (experienceYears >= min) experienceScore = 12; // At least covers min
            else experienceScore = 5; // Junior for this role
        } else {
            // Fallback to static if no range found
            if (experienceYears >= 5) experienceScore = 25;
            else if (experienceYears >= 3) experienceScore = 18;
            else if (experienceYears >= 1) experienceScore = 10;
        }

        // Direct Role Boost (Max 35) - If resume mentions the exact Job Title
        int roleBoost = 0;
        String lowerResume = resumeText.toLowerCase();
        String lowerTitle = jobDescription.split("\n")[0].toLowerCase().replace(" engineer", "").replace(" developer", "").trim();
        
        // Dynamic Role Title Matching (e.g. "Software Engineer" vs "Software")
        if (!lowerTitle.isEmpty() && lowerResume.contains(lowerTitle)) {
            roleBoost = 35;
        } else {
            // Partial Role Check against known role lists
            for (String roleName : ROLE_SKILLS.keySet()) {
                String cleanRole = roleName.toLowerCase().replace(" engineer", "").replace(" developer", "");
                if (lowerResume.contains(cleanRole) && jobDescription.toLowerCase().contains(roleName.toLowerCase())) {
                    roleBoost = 25;
                    break;
                }
            }
        }

        // Recency Boost (Max 10)
        int recencyBoost = 0;
        if (!matchedSkills.isEmpty()) {
            long recentMatches = matchedSkills.stream()
                .filter(lowerResume::contains)
                .count();
            recencyBoost = (int) Math.min(10, (recentMatches * 10.0) / matchedSkills.size());
        }

        // Core Tech Match (Max 15) - Give points for general 'Developer/Engineer' roles
        // This ensures nobody gets a flat 0% if they have professional titles on their resume
        int coreTechScore = 0;
        Set<String> coreTerms = DOMAIN_SKILLS.get("General Tech & Roles");
        long resumeCoreCount = coreTerms.stream().filter(lowerResume::contains).count();
        if (resumeCoreCount > 0) coreTechScore = (int) Math.min(15, 5 + (resumeCoreCount * 2));

        // Domain Relevance (Max 15): NEW Domain-Aware Logic
        // Find if this job matches any of our DOMAIN_SKILLS categories
        String jobDomain = "General Tech & Roles";
        int mostMatches = 0;
        for (String domain : DOMAIN_SKILLS.keySet()) {
            if (domain.equals("General Tech & Roles")) continue;
            long matches = DOMAIN_SKILLS.get(domain).stream()
                .filter(jobDescription.toLowerCase()::contains)
                .count();
            if (matches > mostMatches) {
                mostMatches = (int) matches;
                jobDomain = domain;
            }
        }
        
        // Targeted Domain Boost: If job is C++, use only the user's C++ Domain Fit
        int jobDomainFit = domainFit.getOrDefault(jobDomain, 0);
        int domainScore = (jobDomainFit > 0) ? (5 + (jobDomainFit / 4)) : 0; // Up to ~13-15 points

        // Penalty (Minimal: 1 per missing, cap at 10)
        int penalty = Math.min(10, missingMandatorySkills.size() * 1);

        int finalScore = skillScore + experienceScore + roleBoost + recencyBoost + domainScore + coreTechScore - penalty;
        
        // Final Alignment: If Domain Fit for THIS job is extremely low (under 10%), cap the score
        // This prevents "Python" jobs from getting 80%+ if the user is a C++ expert
        if (jobDomainFit < 10 && !jobDomain.equals("General Tech & Roles")) {
            finalScore = Math.min(finalScore, 45); 
        }

        // Visibility Floor
        if (!matchedSkills.isEmpty() && finalScore < 15) finalScore = 15;
        
        finalScore = Math.max(0, Math.min(finalScore, 100));

        /* ---------- SUMMARY ---------- */
        rankSummary.add("Analyzed " + mandatorySkills.size() + " mandatory job skills");
        rankSummary.add("Confidence: Found " + matchedSkills.size() + " skills in high-impact sections (Experience/Projects)");
        rankSummary.add("Experience: " + experienceYears + " years total");
        rankSummary.add("Missing mandatory: " + missingMandatorySkills.size());

        /* ---------- SKILL GAP PRIORITY ---------- */
        SkillGapPriority priority;
        if (missingMandatorySkills.size() >= 4) priority = SkillGapPriority.HIGH;
        else if (missingMandatorySkills.size() >= 2) priority = SkillGapPriority.MEDIUM;
        else priority = SkillGapPriority.LOW;

        /* ---------- LEARNING ROADMAP ---------- */
        List<String> learningRoadmap = missingMandatorySkills.stream()
                .limit(6)
                .map(skill -> "Action: Master " + skill + " through a focused project")
                .collect(Collectors.toList());

        return new ResumeScore(
                fileName,
                finalScore,
                experienceYears,
                matchedSkills,
                missingSkills,
                missingMandatorySkills,
                "Deep analysis based on sectional weighting, project context, and recency boosts.",
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

    /* ---------- JD EXPERIENCE RANGE EXTRACTION ---------- */
    private int[] extractExperienceRangeFromJD(String jd) {
        Pattern p = Pattern.compile("(\\d+)-(\\d+)\\s*(years?|yrs?)");
        Matcher m = p.matcher(jd.toLowerCase());
        if (m.find()) {
            return new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
        }
        return null;
    }

    /* ---------- JD SKILL EXTRACTION ---------- */
    private Set<String> extractMandatorySkills(String jobDescription) {
        Set<String> mandatory = new HashSet<>();
        String lowerJD = jobDescription.toLowerCase();

        for (Set<String> skills : DOMAIN_SKILLS.values()) {
            for (String skill : skills) {
                // Word boundary check to avoid substring issues (e.g., 'c' in 'code')
                Pattern p = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b", Pattern.CASE_INSENSITIVE);
                if (p.matcher(lowerJD).find()) {
                    mandatory.add(skill);
                }
            }
        }
        
        // Final fallback: if no technical keys found, look for very general role terms
        if (mandatory.isEmpty()) {
            if (lowerJD.contains("job") || lowerJD.contains("role") || lowerJD.contains("looking")) {
                mandatory.add("software"); // Baseline keyword
            }
        }
        
        return mandatory;
    }
}