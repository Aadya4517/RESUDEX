package com.resudex.model;

import java.util.Set;

public class ResumeScore {

    private String fileName;
    private int finalScore;
    private int experienceYears;
    private Set<String> matchedSkills;

    public ResumeScore(String fileName, int finalScore, int experienceYears, Set<String> matchedSkills) {
        this.fileName = fileName;
        this.finalScore = finalScore;
        this.experienceYears = experienceYears;
        this.matchedSkills = matchedSkills;
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
}