package com.resudex.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResumeScore {

    private String fileName;
    private int finalScore;
    private int experienceYears;

    private Set<String> matchedSkills;
    private Set<String> missingSkills;
    private Set<String> missingMandatorySkills;

    private String explanation;

    private int completenessScore;
    private Set<String> missingSections;

    private SkillGapPriority skillGapPriority;
    private List<String> learningRoadmap;

    private Map<String, Integer> roleFit;
    private Map<String, Integer> domainFit;

    private List<String> rankSummary;

    public ResumeScore(
            String fileName,
            int finalScore,
            int experienceYears,
            Set<String> matchedSkills,
            Set<String> missingSkills,
            Set<String> missingMandatorySkills,
            String explanation,
            int completenessScore,
            Set<String> missingSections,
            SkillGapPriority skillGapPriority,
            List<String> learningRoadmap,
            Map<String, Integer> roleFit,
            Map<String, Integer> domainFit,
            List<String> rankSummary
    ) {
        this.fileName = fileName;
        this.finalScore = finalScore;
        this.experienceYears = experienceYears;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.missingMandatorySkills = missingMandatorySkills;
        this.explanation = explanation;
        this.completenessScore = completenessScore;
        this.missingSections = missingSections;
        this.skillGapPriority = skillGapPriority;
        this.learningRoadmap = learningRoadmap;
        this.roleFit = roleFit;
        this.domainFit = domainFit;
        this.rankSummary = rankSummary;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public Set<String> getMatchedSkills() {
        return matchedSkills;
    }

    public Set<String> getMissingSkills() {
        return missingSkills;
    }

    public Set<String> getMissingMandatorySkills() {
        return missingMandatorySkills;
    }

    public String getExplanation() {
        return explanation;
    }

    public int getCompletenessScore() {
        return completenessScore;
    }

    public Set<String> getMissingSections() {
        return missingSections;
    }

    public SkillGapPriority getSkillGapPriority() {
        return skillGapPriority;
    }

    public List<String> getLearningRoadmap() {
        return learningRoadmap;
    }

    public Map<String, Integer> getRoleFit() {
        return roleFit;
    }

    public Map<String, Integer> getDomainFit() {
        return domainFit;
    }

    public List<String> getRankSummary() {
        return rankSummary;
    }
}