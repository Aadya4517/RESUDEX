package com.resudex.model;

import java.util.Set;

public class ResumeScore {

    private String fileName;
    private int score;
    private Set<String> matchedSkills;

    public ResumeScore(String fileName, int score, Set<String> matchedSkills) {
        this.fileName = fileName;
        this.score = score;
        this.matchedSkills = matchedSkills;
    }

    public String getFileName() {
        return fileName;
    }

    public int getScore() {
        return score;
    }

    public Set<String> getMatchedSkills() {
        return matchedSkills;
    }
}